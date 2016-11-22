package controllers

import javax.inject.Inject

import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNel
import cats.syntax.validated._
import controllers.FieldCheckHelpers.FieldErrors
import forms.validation.{CostItem, CostItemValidator, CostItemValues, FieldError}
import models.{ApplicationId, ApplicationSectionDetail}
import play.api.libs.json._
import play.api.mvc.{Action, Controller, Result}
import services.ApplicationOps

import scala.concurrent.{ExecutionContext, Future}

class CostController @Inject()(actionHandler: ActionHandler, applications: ApplicationOps)(implicit ec: ExecutionContext)
  extends Controller with ApplicationResults {

  implicit val costItemValuesF = Json.format[CostItemValues]
  implicit val costItemF = Json.format[CostItem]

  def addItem(applicationId: ApplicationId, sectionNumber: Int) = Action.async { implicit request =>
    showItemForm(applicationId, sectionNumber, JsObject(List.empty), List.empty)
  }

  def validateItem(o: JsObject): ValidatedNel[FieldError, CostItem] = {
    (o \ "item").validate[CostItemValues] match {
      case JsError(errs) => FieldError("item", s"could not convert $o to CostItemValues").invalidNel
      case JsSuccess(values, _) =>
        CostItemValidator.validate("item", values)
    }
  }

  def editItem(applicationId: ApplicationId, sectionNumber: Int, itemNumber: Int) = Action.async {
    applications.getItem[JsObject](applicationId, sectionNumber, itemNumber).flatMap {
      case Some(item) => showItemForm(applicationId, sectionNumber, JsObject(Seq("item" -> item)), List.empty, Some(itemNumber))
      case None => Future.successful(BadRequest)
    }
  }

  def saveItem(applicationId: ApplicationId, sectionNumber: Int, itemNumber: Int) = Action.async(JsonForm.parser) { implicit request =>
    validateItem(request.body.values) match {
      case Valid(ci) =>
        val itemJson = Json.toJson(ci).as[JsObject] + ("itemNumber" -> JsNumber(itemNumber))
        val doc = JsObject(Seq("item" -> itemJson))
        applications.saveItem(applicationId, sectionNumber, doc).flatMap {
          case Nil => Future.successful(redirectToSectionForm(applicationId, sectionNumber))
          case errs => showItemForm(applicationId, sectionNumber, request.body.values, errs, Some(itemNumber))
        }
      case Invalid(errs) => showItemForm(applicationId, sectionNumber, request.body.values, errs.toList, Some(itemNumber))
    }
  }

  def createItem(applicationId: ApplicationId, sectionNumber: Int) = Action.async(JsonForm.parser) { implicit request =>
    validateItem(request.body.values) match {
      case Valid(ci) => applications.saveItem(applicationId, sectionNumber, JsObject(Seq("item" -> Json.toJson(ci)))).flatMap {
        case Nil => Future.successful(redirectToSectionForm(applicationId, sectionNumber))
        case errs => showItemForm(applicationId, sectionNumber, request.body.values, errs)
      }
      case Invalid(errs) => showItemForm(applicationId, sectionNumber, request.body.values, errs.toList)
    }
  }

  def deleteItem(applicationId: ApplicationId, sectionNumber: Int, itemNumber: Int) = Action.async {
    applications.deleteItem(applicationId, sectionNumber, itemNumber).flatMap { _ =>
      // Check if we deleted the last item in the list and, if so, delete the section so
      // it will go back to the Not Started state.
      applications.getSection(applicationId, sectionNumber).flatMap {
        case Some(s) if (s.answers \ "items").validate[JsArray].asOpt.getOrElse(JsArray(List.empty)).value.isEmpty =>
          applications.deleteSection(applicationId, sectionNumber).map { _ =>
            redirectToSectionForm(applicationId, sectionNumber)
          }
        case _ => Future.successful(redirectToSectionForm(applicationId, sectionNumber))
      }
    }
  }

  def showItemForm(applicationId: ApplicationId, sectionNumber: Int, doc: JsObject, errs: FieldErrors, itemNumber:Option[Int] = None): Future[Result] = {
    import ApplicationData._
    import FieldCheckHelpers._

    val fields = itemFieldsFor(sectionNumber).getOrElse(List.empty)
    val checks = itemChecksFor(sectionNumber)
    val hints = hinting(doc, checks)

    actionHandler.gatherSectionDetails(applicationId, sectionNumber).map {
      case Some(app) =>
        Ok(views.html.costItemForm(app, fields, app.formSection.questionMap, doc, errs, hints, cancelLink(app, sectionNumber), itemNumber))
      case None => NotFound
    }
  }

  def cancelLink(app: ApplicationSectionDetail, sectionNumber: Int): String = {
    val items = app.section.flatMap(s => (s.answers \ "items").validate[JsArray].asOpt).getOrElse(JsArray(List.empty)).value
    if (items.isEmpty) controllers.routes.ApplicationController.show(app.id).url
    else sectionFormCall(app.id, sectionNumber).url
  }
}
