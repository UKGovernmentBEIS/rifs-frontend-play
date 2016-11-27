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

  def doSave(id: ApplicationId, sectionNumber: Int, fieldValues: JsObject): Future[Result] = {
    gatherSectionDetails(id, sectionNumber).flatMap {
      case Some(app) =>
        app.formSection.sectionType match {
          case "form" =>
            JsonHelpers.allFieldsEmpty(fieldValues) match {
              case true => applications.deleteSection(id, sectionNumber).map(_ => redirectToOverview(id))
              case false => applications.saveSection(id, sectionNumber, fieldValues).map(_ => redirectToOverview(id))
            }
          case "list" => Future.successful(redirectToOverview(id))
        }
      case None => Future.successful(NotFound)
    }
  }

  def doComplete(id: ApplicationId, sectionNumber: Int, fieldValues: JsObject): Future[Result] = {
    gatherSectionDetails(id, sectionNumber).flatMap {
      case Some(app) =>
        val answersF: Future[Option[JsObject]] = app.formSection.sectionType match {
          case "form" => Future.successful(Some(fieldValues))
          // Instead of using the values that were passed in from the form we'll use the values that
          // have already been saved against the item list, since these were created by the add-item
          // form.
          case "list" => applications.getSection(id, sectionNumber).map(_.map(_.answers))
        }

        answersF.flatMap {
          case Some(answers) =>
            applications.completeSection(id, sectionNumber, answers).flatMap {
              case Nil => Future.successful(redirectToOverview(id))
              case errs => redisplaySectionForm(id, sectionNumber, answers, errs)
            }
          case None => Future.successful(NotFound)
        }
      case None => Future.successful(NotFound)
    }
  }

  def doSaveItem(id: ApplicationId, sectionNumber: Int, fieldValues: JsObject): Future[Result] = {
    JsonHelpers.allFieldsEmpty(fieldValues) match {
      case true => applications.deleteSection(id, sectionNumber).map(_ => redirectToOverview(id))
      case false => applications.saveItem(id, sectionNumber, fieldValues).flatMap {
        case Nil => Future.successful(redirectToOverview(id))
        case errs => redisplaySectionForm(id, sectionNumber, fieldValues, errs)
      }
    }
  }

  def doPreview(id: ApplicationId, sectionNumber: Int, fieldValues: JsObject): Future[Result] = {
    gatherSectionDetails(id, sectionNumber).flatMap {
      case Some(app) =>
        app.formSection.sectionType match {
          case "form" =>
            val errs = check(fieldValues, previewChecksFor(app.formSection))
            if (errs.isEmpty) {
              applications.saveSection(id, sectionNumber, fieldValues).map { _ =>
                redirectToPreview(id, sectionNumber)
              }
            } else redisplaySectionForm(id, sectionNumber, fieldValues, errs)

          case "list" => Future.successful(redirectToPreview(id, sectionNumber))
        }
      case None => Future.successful(NotFound)
    }
  }

  def doSubmit(id: ApplicationId): Future[Option[SubmittedApplicationRef]] = {
    applications.submit(id)
  }

  def completeAndPreview(id: ApplicationId, sectionNumber: Int, fieldValues: JsObject): Future[Result] = {
    gatherSectionDetails(id, sectionNumber).flatMap {
      case Some(app) =>
        val answersF: Future[Option[JsObject]] = app.formSection.sectionType match {
          case "form" => Future.successful(Some(fieldValues))
          case "list" => applications.getSection(id, sectionNumber).map(_.map(_.answers))
        }

        answersF.flatMap {
          case Some(answers) =>
            val previewCheckErrs = check(answers, previewChecksFor(app.formSection))
            if (previewCheckErrs.isEmpty) {
              JsonHelpers.allFieldsEmpty(answers) match {
                case true => applications.deleteSection(id, sectionNumber).map(_ => redirectToOverview(id))
                case false => applications.completeSection(id, sectionNumber, answers).flatMap {
                  case Nil => Future.successful(redirectToPreview(id, sectionNumber))
                  case errs => redisplaySectionForm(id, sectionNumber, answers, errs)
                }
              }
            } else redisplaySectionForm(id, sectionNumber, answers, previewCheckErrs)

          case None => Future.successful(NotFound)
        }
      case None => Future.successful(NotFound)
    }
  }

  def redirectToPreview(id: ApplicationId, sectionNumber: Int) =
    Redirect(routes.ApplicationPreviewController.previewSection(id, sectionNumber))

  def renderSectionForm(app: ApplicationSectionDetail,
                        sectionNumber: Int,
                        errs: FieldErrors,
                        hints: FieldHints): Result = {
    val answers = app.section.map { s => s.answers }.getOrElse(JsObject(List.empty))
    selectSectionForm(app, sectionNumber, answers, errs)
  }

  def redisplaySectionForm(id: ApplicationId, sectionNumber: Int, answers: JsObject, errs: FieldErrors = noErrors): Future[Result] = {
    val ft = gatherSectionDetails(id, sectionNumber)

    ft.map {
      case (Some(app)) => selectSectionForm(app, sectionNumber, answers, errs)
      case (None) => NotFound
    }
  }

  def selectSectionForm(app: ApplicationSectionDetail, sectionNumber: Int, answers: JsObject, errs: FieldErrors): Result = {
    val checks = app.formSection.fields.map(f => f.name -> f.check).toMap
    val hints = hinting(answers, checks)

    app.formSection.sectionType match {
      case "form" => Ok(views.html.sectionForm(app, answers, errs, hints))
      case "list" =>
        answers \ "items" match {
          case JsDefined(JsArray(is)) if is.nonEmpty =>
            val itemValues: Seq[JsValue] = (answers \ "items").validate[JsArray].asOpt.map(_.value).getOrElse(Seq())
            val costItems = itemValues.flatMap(_.validate[CostItem].asOpt)
            Ok(views.html.sectionList(app, costItems, answers, errs, hints))
          case _ => Redirect(controllers.routes.CostController.addItem(app.id, sectionNumber))
        }
    }
  }

  def gatherSectionDetails(id: ApplicationId, sectionNumber: Int): Future[Option[ApplicationSectionDetail]] =
    applications.sectionDetail(id, sectionNumber)

  def previewChecksFor(formSection: ApplicationFormSection): Map[String, FieldCheck] =
    formSection.fields.map(f => f.name -> f.previewCheck).toMap
}
