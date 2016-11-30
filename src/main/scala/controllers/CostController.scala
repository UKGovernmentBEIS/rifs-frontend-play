package controllers

import javax.inject.Inject

import actions.AppSectionAction
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

class CostController @Inject()(
                                actionHandler: ActionHandler,
                                applications: ApplicationOps,
                                AppSectionAction: AppSectionAction
                              )(implicit ec: ExecutionContext)
  extends Controller with ApplicationResults {

  implicit val costItemValuesF = Json.format[CostItemValues]
  implicit val costItemF = Json.format[CostItem]

  def addItem(applicationId: ApplicationId, sectionNumber: Int) = AppSectionAction(applicationId, sectionNumber) { implicit request =>
    showItemForm(request.appSection, JsObject(List.empty), List.empty)
  }

  def validateItem(o: JsObject): ValidatedNel[FieldError, CostItem] = {
    (o \ "item").validate[CostItemValues] match {
      case JsError(errs) => FieldError("item", s"could not convert $o to CostItemValues").invalidNel
      case JsSuccess(values, _) =>
        CostItemValidator.validate("item", values)
    }
  }

  def editItem(applicationId: ApplicationId, sectionNumber: Int, itemNumber: Int) = AppSectionAction(applicationId, sectionNumber) { request =>
    val itemO = request.appSection.section.flatMap { s =>
      findItem(s.answers, itemNumber)
    }

    itemO match {
      case Some(item) => showItemForm(request.appSection, JsObject(Seq("item" -> item)), List.empty, Some(itemNumber))
      case None => BadRequest
    }
  }


  private def hasItemNumber(o: JsObject, num: Int) = o \ "itemNumber" match {
    case JsDefined(JsNumber(n)) if n == num => true
    case _ => false
  }

  private def findItem(doc: JsObject, itemNumber: Int) = {
    val items = doc \ "items" match {
      case JsDefined(JsArray(is)) => is.collect { case o: JsObject => o }
      case _ => Seq()
    }
    items.find(o => hasItemNumber(o, itemNumber))
  }

  def saveItem(applicationId: ApplicationId, sectionNumber: Int, itemNumber: Int) = AppSectionAction(applicationId, sectionNumber).async(JsonForm.parser) { implicit request =>
    validateItem(request.body.values) match {
      case Valid(ci) =>
        val itemJson = Json.toJson(ci).as[JsObject] + ("itemNumber" -> JsNumber(itemNumber))
        val doc = JsObject(Seq("item" -> itemJson))
        applications.saveItem(applicationId, sectionNumber, doc).map {
          case Nil => redirectToSectionForm(applicationId, sectionNumber)
          case errs => showItemForm(request.appSection, request.body.values, errs, Some(itemNumber))
        }
      case Invalid(errs) => Future.successful(showItemForm(request.appSection, request.body.values, errs.toList, Some(itemNumber)))
    }
  }

  def createItem(applicationId: ApplicationId, sectionNumber: Int) = AppSectionAction(applicationId, sectionNumber).async(JsonForm.parser) { implicit request =>
    validateItem(request.body.values) match {
      case Valid(ci) => applications.saveItem(request.appSection.id, request.appSection.sectionNumber, JsObject(Seq("item" -> Json.toJson(ci)))).map {
        case Nil => redirectToSectionForm(applicationId, sectionNumber)
        case errs => showItemForm(request.appSection, request.body.values, errs)
      }
      case Invalid(errs) => Future.successful(showItemForm(request.appSection, request.body.values, errs.toList))
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

  def showItemForm(app: ApplicationSectionDetail, doc: JsObject, errs: FieldErrors, itemNumber: Option[Int] = None): Result = {
    import ApplicationData._
    import FieldCheckHelpers._

    val fields = itemFieldsFor(app.sectionNumber).getOrElse(List.empty)
    val checks = itemChecksFor(app.sectionNumber)
    val hints = hinting(doc, checks)

    Ok(views.html.costItemForm(app, fields, app.formSection.questionMap, doc, errs, hints, cancelLink(app), itemNumber))
  }

  def cancelLink(app: ApplicationSectionDetail): String = {
    val items = app.section.flatMap(s => (s.answers \ "items").validate[JsArray].asOpt).getOrElse(JsArray(List.empty)).value
    if (items.isEmpty) controllers.routes.ApplicationController.show(app.id).url
    else sectionFormCall(app.id, app.sectionNumber).url
  }
}
