package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import forms.Field
import forms.validation.CostItemValues
import models._
import play.api.Logger
import play.api.libs.json.{JsArray, JsDefined, JsObject}
import play.api.mvc.Result
import play.api.mvc.Results._
import services.{ApplicationFormOps, ApplicationOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

class ActionHandler @Inject()(applications: ApplicationOps, applicationForms: ApplicationFormOps, opportunities: OpportunityOps)(implicit ec: ExecutionContext) {

  import ApplicationData._
  import FieldCheckHelpers._

  def doSave(id: ApplicationId, sectionNumber: Int, fieldValues: JsObject): Future[Result] =
    applications.saveSection(id, sectionNumber, fieldValues).map { _ =>
      Redirect(routes.ApplicationController.show(id))
    }

  def doComplete(id: ApplicationId, sectionNumber: Int, fieldValues: JsObject): Future[Result] =
    applications.completeSection(id, sectionNumber, fieldValues).flatMap {
      case Nil => Future.successful(Redirect(routes.ApplicationController.show(id)))
      case errs => redisplaySectionForm(id, sectionNumber, JsonHelpers.flatten("", fieldValues), errs)
    }

  def doSaveItem(id: ApplicationId, sectionNumber: Int, fieldValues: JsObject): Future[Result] = {
    applications.saveItem(id, sectionNumber, fieldValues).flatMap {
      case Nil => Future.successful(Redirect(routes.ApplicationController.show(id)))
      case errs => redisplaySectionForm(id, sectionNumber, JsonHelpers.flatten("", fieldValues), errs)
    }
  }

  def doPreview(id: ApplicationId, sectionNumber: Int, fieldValues: JsObject): Future[Result] = {
    val errs = check(fieldValues, previewChecksFor(sectionNumber))
    if (errs.isEmpty) {
      applications.saveSection(id, sectionNumber, fieldValues).map { _ =>
        Redirect(routes.ApplicationPreviewController.previewSection(id, sectionNumber))
      }
    } else redisplaySectionForm(id, sectionNumber, JsonHelpers.flatten("", fieldValues), errs)
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
    val hints = section.map(s => hinting(s.answers, checksFor(sectionNumber))).getOrElse(List())

    sectionTypeFor(sectionNumber) match {
      case VanillaSection => Ok(views.html.sectionForm(app, appForm, section, formSection, opp, fields, questions, answers, errs, hints))
      case CostSection =>
        val sectionDoc = section.map(_.answers).getOrElse(JsObject(Seq()))
        val cancelLink = controllers.routes.ApplicationController.show(app.id)
        sectionDoc \ "items" match {
          case JsDefined(JsArray(is)) =>
            Logger.debug(is.toString)
            val costItems = is.flatMap(_.validate[CostItemValues].asOpt)
            Logger.debug(costItems.toString())
            if (costItems.nonEmpty) Ok(views.html.costSectionList(app, appForm, formSection, opp, costItems.toList, questionsFor(sectionNumber)))
            else {Ok(views.html.costItemForm(app, appForm, formSection, opp, fields, questions, answers, errs, hints, cancelLink, None))}
          case _ => Ok(views.html.costItemForm(app, appForm, formSection, opp, fields, questions, answers, errs, hints, cancelLink, None))
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
