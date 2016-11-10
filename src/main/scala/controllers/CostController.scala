package controllers

import javax.inject.Inject

import cats.data.Validated.{Invalid, Valid}
import cats.data.{OptionT, ValidatedNel}
import cats.instances.future._
import cats.syntax.validated._
import controllers.FieldCheckHelpers.FieldErrors
import forms.validation.{CostItem, CostItemValidator, CostItemValues, FieldError}
import models.{ApplicationId, ApplicationOverview}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, Controller, Result}
import services.ApplicationOps

import scala.concurrent.{ExecutionContext, Future}

class CostController @Inject()(actionHandler: ActionHandler, applications: ApplicationOps)(implicit ec: ExecutionContext)
  extends Controller with ApplicationResults {

  implicit val costItemValuesF = Json.format[CostItemValues]
  implicit val costItemF = Json.format[CostItem]

  def addItem(applicationId: ApplicationId, sectionNumber: Int) = Action.async { implicit request =>
    showItemForm(applicationId, sectionNumber, JsObject(Seq()), List())
  }

  def validateItem(o: JsObject): ValidatedNel[FieldError, CostItem] = {
    (o \ "item").validate[CostItemValues] match {
      case JsError(errs) => FieldError("item", s"could not convert $o to CostItemValues").invalidNel
      case JsSuccess(values, _) =>
        Logger.debug(values.toString)
        CostItemValidator.validate("item", values)
    }
  }

  def saveItem(applicationId: ApplicationId, sectionNumber: Int) = Action.async(JsonForm.parser) { implicit request =>
    validateItem(request.body.values) match {
      case Valid(ci) => applications.saveItem(applicationId, sectionNumber, JsObject(Seq("item" -> Json.toJson(ci)))).flatMap {
        case Nil => Future.successful(redirectToSectionForm(applicationId, sectionNumber))
        case errs => showItemForm(applicationId, sectionNumber, request.body.values, errs)
      }
      case Invalid(errs) => showItemForm(applicationId, sectionNumber, request.body.values, errs.toList)
    }
  }

  def deleteItem(applicationId: ApplicationId, sectionNumber: Int, itemNumber: Int) = Action.async {
    applications.deleteItem(applicationId, sectionNumber, itemNumber).map { _ =>
      redirectToSectionForm(applicationId, sectionNumber)
    }
  }

  def showItemForm(applicationId: ApplicationId, sectionNumber: Int, doc: JsObject, errs: FieldErrors): Future[Result] = {
    val details1 = actionHandler.gatherApplicationDetails(applicationId)

    val details2 = for {
      ds <- OptionT(details1)
      fs <- OptionT.fromOption[Future](ds._2.sections.find(_.sectionNumber == sectionNumber))
    } yield (ds, fs)

    import ApplicationData._
    import FieldCheckHelpers._

    val questions = questionsFor(sectionNumber)
    val fields = fieldsFor(sectionNumber).getOrElse(Seq())
    val checks = itemChecksFor(sectionNumber)
    val hints = hinting(doc, checks)
    val answers = JsonHelpers.flatten("", doc)

    details2.value.map {
      case Some(((overview, form, opp), fs)) =>
        Ok(views.html.costItemForm(overview, form, fs, opp, fields, questions, answers, errs, hints, cancelLink(applicationId, overview, sectionNumber), None))
      case None => NotFound
    }
  }

  def cancelLink(applicationId: ApplicationId, overview: ApplicationOverview, sectionNumber: Int): String = {
    val items = overview.sections.find(_.sectionNumber == sectionNumber).flatMap(s => (s.answers \ "items").validate[JsArray].asOpt).getOrElse(JsArray(Seq())).value
    if (items.isEmpty) controllers.routes.ApplicationController.show(applicationId).url
    else sectionFormCall(applicationId, sectionNumber).url
  }
}
