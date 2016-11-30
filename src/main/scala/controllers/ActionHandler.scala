package controllers

import javax.inject.Inject

import forms.validation.CostItem
import models._
import play.api.libs.json.{JsArray, JsDefined, JsObject, JsValue}
import play.api.mvc.Result
import play.api.mvc.Results._
import services.{ApplicationFormOps, ApplicationOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

class ActionHandler @Inject()(applications: ApplicationOps, applicationForms: ApplicationFormOps, opportunities: OpportunityOps)(implicit ec: ExecutionContext)
  extends ApplicationResults {

  import ApplicationData._
  import FieldCheckHelpers._

  def doSave(app: ApplicationSectionDetail, fieldValues: JsObject): Future[Result] = {
    app.formSection.sectionType match {
      case SectionTypeForm =>
        JsonHelpers.allFieldsEmpty(fieldValues) match {
          case true => applications.deleteSection(app.id, app.sectionNumber).map(_ => redirectToOverview(app.id))
          case false => applications.saveSection(app.id, app.sectionNumber, fieldValues).map(_ => redirectToOverview(app.id))
        }
      case SectionTypeList => Future.successful(redirectToOverview(app.id))
    }
  }

  def doComplete(app: ApplicationSectionDetail, fieldValues: JsObject): Future[Result] = {
    val answers = app.formSection.sectionType match {
      case SectionTypeForm => fieldValues
      // Instead of using the values that were passed in from the form we'll use the values that
      // have already been saved against the item list, since these were created by the add-item
      // form.
      case SectionTypeList => app.section.map(_.answers).getOrElse(JsObject(Seq()))
    }

    applications.completeSection(app.id, app.sectionNumber, answers).map {
      case Nil => redirectToOverview(app.id)
      case errs => redisplaySectionForm(app, answers, errs)
    }
  }

  def doSaveItem(app: ApplicationSectionDetail, fieldValues: JsObject): Future[Result] = {
    JsonHelpers.allFieldsEmpty(fieldValues) match {
      case true => applications.deleteSection(app.id, app.sectionNumber).map(_ => redirectToOverview(app.id))
      case false => applications.saveItem(app.id, app.sectionNumber, fieldValues).flatMap {
        case Nil => Future.successful(redirectToOverview(app.id))
        case errs => Future.successful(redisplaySectionForm(app, fieldValues, errs))
      }
    }
  }

  def doPreview(app: ApplicationSectionDetail, fieldValues: JsObject): Future[Result] = {
    app.formSection.sectionType match {
      case SectionTypeForm =>
        val errs = check(fieldValues, previewChecksFor(app.formSection))
        if (errs.isEmpty) applications.saveSection(app.id, app.sectionNumber, fieldValues).map(_ => redirectToPreview(app.id, app.sectionNumber))
        else Future.successful(redisplaySectionForm(app, fieldValues, errs))

      case SectionTypeList => Future.successful(redirectToPreview(app.id, app.sectionNumber))
    }
  }

  def doSubmit(id: ApplicationId): Future[Option[SubmittedApplicationRef]] = {
    applications.submit(id)
  }

  def completeAndPreview(app: ApplicationSectionDetail, fieldValues: JsObject): Future[Result] = {
    val answers = app.formSection.sectionType match {
      case SectionTypeForm => fieldValues
      // Instead of using the values that were passed in from the form we'll use the values that
      // have already been saved against the item list, since these were created by the add-item
      // form.
      case SectionTypeList => app.section.map(_.answers).getOrElse(JsObject(Seq()))
    }

    val previewCheckErrs = check(answers, previewChecksFor(app.formSection))
    if (previewCheckErrs.isEmpty) {
      JsonHelpers.allFieldsEmpty(answers) match {
        case true => applications.deleteSection(app.id, app.sectionNumber).map(_ => redirectToOverview(app.id))
        case false => applications.completeSection(app.id, app.sectionNumber, answers).map {
          case Nil => redirectToPreview(app.id, app.sectionNumber)
          case errs => redisplaySectionForm(app, answers, errs)
        }
      }
    } else Future.successful(redisplaySectionForm(app, answers, previewCheckErrs))
  }

  def redirectToPreview(id: ApplicationId, sectionNumber: Int) =
    Redirect(routes.ApplicationPreviewController.previewSection(id, sectionNumber))

  def renderSectionForm(app: ApplicationSectionDetail,
                        errs: FieldErrors,
                        hints: FieldHints): Result = {
    val answers = app.section.map { s => s.answers }.getOrElse(JsObject(List.empty))
    selectSectionForm(app, answers, errs)
  }

  def redisplaySectionForm(app: ApplicationSectionDetail, answers: JsObject, errs: FieldErrors = noErrors): Result = {
    selectSectionForm(app, answers, errs)
  }

  def selectSectionForm(app: ApplicationSectionDetail, answers: JsObject, errs: FieldErrors): Result = {
    val checks = app.formSection.fields.map(f => f.name -> f.check).toMap
    val hints = hinting(answers, checks)

    app.formSection.sectionType match {
      case SectionTypeForm => Ok(views.html.sectionForm(app, answers, errs, hints))
      case SectionTypeList =>
        answers \ "items" match {
          case JsDefined(JsArray(is)) if is.nonEmpty =>
            val itemValues: Seq[JsValue] = (answers \ "items").validate[JsArray].asOpt.map(_.value).getOrElse(Seq())
            val costItems = itemValues.flatMap(_.validate[CostItem].asOpt)
            Ok(views.html.sectionList(app, costItems, answers, errs, hints))
          case _ => Redirect(controllers.routes.CostController.addItem(app.id, app.formSection.sectionNumber))
        }
    }
  }

  def previewChecksFor(formSection: ApplicationFormSection): Map[String, FieldCheck] =
    formSection.fields.map(f => f.name -> f.previewCheck).toMap
}
