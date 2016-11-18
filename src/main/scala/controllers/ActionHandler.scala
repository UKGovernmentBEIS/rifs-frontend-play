package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import models._
import play.api.libs.json.{JsArray, JsDefined, JsObject}
import play.api.mvc.Result
import play.api.mvc.Results._
import services.{ApplicationFormOps, ApplicationOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

class ActionHandler @Inject()(applications: ApplicationOps, applicationForms: ApplicationFormOps, opportunities: OpportunityOps)(implicit ec: ExecutionContext)
  extends ApplicationResults {

  import ApplicationData._
  import FieldCheckHelpers._


  def doSave(id: ApplicationId, sectionNumber: Int, fieldValues: JsObject): Future[Result] = {
    sectionTypeFor(sectionNumber) match {
      case VanillaSection =>
        JsonHelpers.allFieldsEmpty(fieldValues) match {
          case true => applications.deleteSection(id, sectionNumber).map(_ => redirectToOverview(id))
          case false => applications.saveSection(id, sectionNumber, fieldValues).map(_ => redirectToOverview(id))
        }
      case ItemSection => Future.successful(redirectToOverview(id))
    }
  }

  def doComplete(id: ApplicationId, sectionNumber: Int, fieldValues: JsObject): Future[Result] = {
    val answersF: Future[Option[JsObject]] = sectionTypeFor(sectionNumber) match {
      case VanillaSection => Future.successful(Some(fieldValues))
      // Instead of using the values that were passed in from the form we'll use the values that
      // have already been saved against the item list, since these were created by the add-item
      // form.
      case ItemSection => applications.getSection(id, sectionNumber).map(_.map(_.answers))
    }

    answersF.flatMap {
      case Some(answers) => applications.completeSection(id, sectionNumber, answers).flatMap {
        case Nil => Future.successful(redirectToOverview(id))
        case errs => redisplaySectionForm(id, sectionNumber, answers, errs)
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
    sectionTypeFor(sectionNumber) match {
      case VanillaSection =>
        val errs = check(fieldValues, previewChecksFor(sectionNumber))
        if (errs.isEmpty) {
          applications.saveSection(id, sectionNumber, fieldValues).map { _ =>
            redirectToPreview(id, sectionNumber)
          }
        } else redisplaySectionForm(id, sectionNumber, fieldValues, errs)

      case ItemSection => Future.successful(redirectToPreview(id, sectionNumber))
    }
  }

  def doSubmit(id: ApplicationId): Future[Option[SubmittedApplicationRef]] = {
    applications.submit(id)
  }

  def completeAndPreview(id: ApplicationId, sectionNumber: Int, fieldValues: JsObject): Future[Result] = {
    val answersF: Future[Option[JsObject]] = sectionTypeFor(sectionNumber) match {
      case VanillaSection => Future.successful(Some(fieldValues))
      case ItemSection => applications.getSection(id, sectionNumber).map(_.map(_.answers))
    }

    answersF.flatMap {
      case Some(answers) =>
        val previewCheckErrs = check(answers, previewChecksFor(sectionNumber))
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
  }

  def redirectToPreview(id: ApplicationId, sectionNumber: Int) =
    Redirect(routes.ApplicationPreviewController.previewSection(id, sectionNumber))

  def renderSectionForm(id: ApplicationId,
                        sectionNumber: Int,
                        section: Option[ApplicationSection],
                        questions: Map[String, Question],
                        errs: FieldErrors,
                        hints: FieldHints): Future[Result] = {
    val answers = section.map { s => s.answers }.getOrElse(JsObject(List.empty))

    gatherSectionDetails(id, sectionNumber).map {
      case Some((app, appFormSection)) =>
        selectSectionForm(sectionNumber, section, appFormSection, answers, errs, app)
      case None => NotFound
    }
  }

  def redisplaySectionForm(id: ApplicationId, sectionNumber: Int, answers: JsObject, errs: FieldErrors = noErrors): Future[Result] = {
    val ft = gatherSectionDetails(id, sectionNumber)
    val sectionF = applications.getSection(id, sectionNumber)

    for (appDetails <- ft; section <- sectionF) yield (appDetails, section) match {
      case (Some((app, appFormSection)), s) => selectSectionForm(sectionNumber, s, appFormSection, answers, errs, app)
      case (None, _) => NotFound
    }
  }

  def selectSectionForm(sectionNumber: Int,
                        section: Option[ApplicationSection],
                        appFormSection: ApplicationFormSection,
                        answers: JsObject,
                        errs: FieldErrors,
                        app: ApplicationDetail): Result = {
    val hints = hinting(answers, checksFor(sectionNumber))

    sectionTypeFor(sectionNumber) match {
      case VanillaSection => Ok(views.html.sectionForm(app, section, appFormSection, answers, errs, hints))
      case ItemSection =>
        answers \ "items" match {
          case JsDefined(JsArray(is)) if is.nonEmpty => Ok(views.html.sectionForm(app, section, appFormSection, answers, errs, hints))
          case _ => Redirect(controllers.routes.CostController.addItem(app.id, sectionNumber))
        }
    }
  }

  def gatherSectionDetails(id: ApplicationId, sectionNumber: Int): Future[Option[(ApplicationDetail, ApplicationFormSection)]] = {
    for {
      a <- OptionT(applications.detail(id))
      fs <- OptionT.fromOption(a.applicationForm.sections.find(_.sectionNumber == sectionNumber))
    } yield (a, fs)
  }.value
}
