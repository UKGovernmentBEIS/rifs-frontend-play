package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import forms.Field
import forms.validation.CostItem
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
          case true => applications.deleteSection(id, sectionNumber).map { _ =>
            Redirect(routes.ApplicationController.show(id))
          }
          case false => applications.saveSection(id, sectionNumber, fieldValues).map { _ =>
            Redirect(routes.ApplicationController.show(id))
          }
        }
      case CostSection => Future.successful(redirectToOverview(id))
    }
  }

  def doComplete(id: ApplicationId, sectionNumber: Int, fieldValues: JsObject): Future[Result] =
    sectionTypeFor(sectionNumber) match {
      case VanillaSection => applications.completeSection(id, sectionNumber, fieldValues).flatMap {
        case Nil => Future.successful(Redirect(routes.ApplicationController.show(id)))
        case errs => redisplaySectionForm(id, sectionNumber, JsonHelpers.flatten("", fieldValues), errs)
      }

      case CostSection =>
        applications.getSection(id, sectionNumber).flatMap {
          case Some(section) =>
            applications.completeSection(id, sectionNumber, section.answers).flatMap {
              case Nil => Future.successful(redirectToOverview(id))
              case errs => redisplaySectionForm(id, sectionNumber, JsonHelpers.flatten("", section.answers), errs)
            }

          case None => Future.successful(NotFound)
        }
    }

  def doSaveItem(id: ApplicationId, sectionNumber: Int, fieldValues: JsObject): Future[Result] = {
    JsonHelpers.allFieldsEmpty(fieldValues) match {
      case true => applications.deleteSection(id, sectionNumber).map { _ =>
        Redirect(routes.ApplicationController.show(id))
      }
      case false => applications.saveItem(id, sectionNumber, fieldValues).flatMap {
        case Nil => Future.successful(redirectToOverview(id))
        case errs => redisplaySectionForm(id, sectionNumber, JsonHelpers.flatten("", fieldValues), errs)
      }
    }
  }

  def doPreview(id: ApplicationId, sectionNumber: Int, fieldValues: JsObject): Future[Result] = {
    sectionTypeFor(sectionNumber) match {
      case VanillaSection =>
        val errs = check(fieldValues, previewChecksFor(sectionNumber))
        if (errs.isEmpty) {
          applications.saveSection(id, sectionNumber, fieldValues).map { _ =>
            Redirect(routes.ApplicationPreviewController.previewSection(id, sectionNumber))
          }
        } else redisplaySectionForm(id, sectionNumber, JsonHelpers.flatten("", fieldValues), errs)

      case CostSection => Future.successful(wip(sectionFormCall(id, sectionNumber).url))
    }
  }

  def completeAndPreview(id: ApplicationId, sectionNumber: Int, fieldValues: JsObject): Future[Result] = {

    sectionTypeFor(sectionNumber) match {
      case VanillaSection =>
        val errs = check(fieldValues, previewChecksFor(sectionNumber))
        if (errs.isEmpty) {
          JsonHelpers.allFieldsEmpty(fieldValues) match {
            case true => applications.deleteSection(id, sectionNumber).map { _ =>
              Redirect(routes.ApplicationController.show(id))
            }
            case false => applications.completeSection(id, sectionNumber, fieldValues).flatMap {
              case Nil => Future.successful(Redirect(routes.ApplicationPreviewController.previewSection(id, sectionNumber)))
              case errs => redisplaySectionForm(id, sectionNumber, JsonHelpers.flatten("", fieldValues), errs)
            }
          }
        }
        else redisplaySectionForm(id, sectionNumber, JsonHelpers.flatten("", fieldValues), errs)
      case CostSection => Future.successful(wip(sectionFormCall(id, sectionNumber).url))
    }
  }

  def redirectToPreview(id: ApplicationId, sectionNumber: Int): Future[Result] = {
    Future.successful(Redirect(controllers.routes.ApplicationPreviewController.previewSection(id, sectionNumber)))
  }

  def renderSectionForm(id: ApplicationId,
                        sectionNumber: Int,
                        section: Option[ApplicationSection],
                        questions: Map[String, Question],
                        fields: Seq[Field],
                        errs: FieldErrors,
                        hints: FieldHints): Future[Result] = {
    val answers = section.map { s => JsonHelpers.flatten("", s.answers) }.getOrElse(Map[String, String]())

    gatherApplicationDetails(id).map {
      case Some((app, appForm, opp)) => selectSectionForm(sectionNumber, section, questions, answers, fields, errs, app, appForm, opp)
      case None => NotFound
    }
  }

  def redisplaySectionForm(id: ApplicationId, sectionNumber: Int, answers: Map[String, String], errs: FieldErrors = noErrors): Future[Result] = {
    val ft = gatherApplicationDetails(id)
    val sectionF = applications.getSection(id, sectionNumber)
    val questions = questionsFor(sectionNumber)

    fieldsFor(sectionNumber) match {
      case Some(fields) =>
        for (x <- ft; section <- sectionF) yield (x, section) match {
          case (Some((app, appForm, opp)), s) => selectSectionForm(sectionNumber, s, questions, answers, fields, errs, app, appForm, opp)
          case (None, _) => NotFound
        }

      case None => Future.successful(Ok(views.html.wip(routes.ApplicationController.show(id).url)))
    }
  }

  def selectSectionForm(sectionNumber: Int, section: Option[ApplicationSection], questions: Map[String, Question], answers: Map[String, String], fields: Seq[Field], errs: FieldErrors, app: ApplicationOverview, appForm: ApplicationForm, opp: Opportunity): Result = {
    val formSection: ApplicationFormSection = appForm.sections.find(_.sectionNumber == sectionNumber).get
    val hints = hinting(JsonHelpers.unflatten(answers), checksFor(sectionNumber))

    sectionTypeFor(sectionNumber) match {
      case VanillaSection => Ok(views.html.sectionForm(app, appForm, section, formSection, opp, fields, questions, answers, errs, hints))
      case CostSection =>
        val sectionDoc = section.map(_.answers).getOrElse(JsObject(Seq()))
        val cancelLink = controllers.routes.ApplicationController.show(app.id)
        sectionDoc \ "items" match {
          case JsDefined(JsArray(is)) =>
            val costItems = is.flatMap(_.validate[CostItem].asOpt)
            if (costItems.nonEmpty) Ok(views.html.costSectionList(app, appForm, section, formSection, opp, costItems.toList, questionsFor(sectionNumber), errs))
            else Redirect(controllers.routes.CostController.addItem(app.id, sectionNumber))
          case _ => Redirect(controllers.routes.CostController.addItem(app.id, sectionNumber))
        }
    }
  }

  def gatherApplicationDetails(id: ApplicationId): Future[Option[(ApplicationOverview, ApplicationForm, Opportunity)]] = {
    for {
      a <- OptionT(applications.overview(id))
      af <- OptionT(applicationForms.byId(a.applicationFormId))
      o <- OptionT(opportunities.byId(af.opportunityId))
    } yield (a, af, o)
  }.value
}
