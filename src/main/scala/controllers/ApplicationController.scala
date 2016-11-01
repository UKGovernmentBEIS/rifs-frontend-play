package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import forms._
import models._
import play.api.libs.json._
import play.api.mvc.{Action, Controller, Result}
import services.{ApplicationFormOps, ApplicationOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

class ApplicationController @Inject()(applications: ApplicationOps, applicationForms: ApplicationFormOps, opportunities: OpportunityOps)(implicit ec: ExecutionContext)
  extends Controller {

  def showOrCreateForForm(id: ApplicationFormId) = Action.async {
    applications.getOrCreateForForm(id).map {
      case Some(app) => Redirect(controllers.routes.ApplicationController.show(app.id))
      case None => NotFound
    }
  }

  def show(id: ApplicationId) = Action.async {
    val t = for {
      a <- OptionT(applications.overview(id))
      af <- OptionT(applicationForms.byId(a.applicationFormId))
      opp <- OptionT(opportunities.byId(af.opportunityId))
    } yield (af, a, opp)

    t.value.map {
      case Some((form, overview, opp)) => Ok(views.html.showApplicationForm(form, overview, opp))
      case None => NotFound
    }
  }

  import ApplicationData._
  import FieldCheckHelpers._

  def showSectionForm(id: ApplicationId, sectionNumber: Int) = Action.async { request =>
    doShowSectionForm(id, sectionNumber)
  }

  def doShowSectionForm(id: ApplicationId, sectionNumber: Int, errs: FieldErrors = noErrors): Future[Result] = {
    fieldsFor(sectionNumber) match {
      case Some(fields) =>
        applications.getSection(id, sectionNumber).flatMap { section =>
          val hints = section.map(s => hinting(s.answers, checksFor(sectionNumber))).getOrElse(List())
          renderSectionForm(id, sectionNumber, section, questionsFor(sectionNumber), fields, errs, hints)
        }

      // Temporary hack to display the WIP page for sections that we haven't yet coded up
      case None => Future.successful(Ok(views.html.wip(routes.ApplicationController.show(id).url)))
    }
  }

  def renderSectionForm(id: ApplicationId,
                        sectionNumber: Int,
                        section: Option[ApplicationSection],
                        questions: Map[String, Question],
                        fields: Seq[Field],
                        errs: FieldErrors,
                        hints: FieldHints): Future[Result] = {
    val ft = for {
      a <- OptionT(applications.overview(id))
      af <- OptionT(applicationForms.byId(a.applicationFormId))
      o <- OptionT(opportunities.byId(af.opportunityId))
    } yield (a, af, o)

    ft.value.map {
      case Some((app, appForm, opp)) =>
        val formSection: ApplicationFormSection = appForm.sections.find(_.sectionNumber == sectionNumber).get
        val answers = section.map { s => JsonHelpers.flatten("", s.answers) }.getOrElse(Map[String, String]())

        sectionTypeFor(sectionNumber) match {
          case VanillaSection => Ok(views.html.sectionForm(app, appForm, section, formSection, opp, fields, questions, answers, errs, hints))
          case CostSection => Ok(views.html.costSectionForm(app, appForm, section, formSection, opp, fields, questions, answers, errs, hints))
        }
      case None => NotFound
    }
  }

  def redisplaySectionForm(id: ApplicationId, sectionNumber: Int, answers: Map[String, String], errs: FieldErrors = noErrors): Future[Result] = {
    val ft = for {
      a <- OptionT(applications.overview(id))
      af <- OptionT(applicationForms.byId(a.applicationFormId))
      o <- OptionT(opportunities.byId(af.opportunityId))
    } yield (a, af, o)

    val sectionF = applications.getSection(id, sectionNumber)

    fieldsFor(sectionNumber) match {
      case Some(fields) =>
        val questions = questionsFor(sectionNumber)
        val y = for {
          x <- ft.value
          section <- sectionF
        } yield (x, section)

        y.map {
          case (Some((app, appForm, opp)), section) =>
            val formSection: ApplicationFormSection = appForm.sections.find(_.sectionNumber == sectionNumber).get
            val hints = section.map(s => hinting(s.answers, checksFor(sectionNumber))).getOrElse(List())

            sectionTypeFor(sectionNumber) match {
              case VanillaSection => Ok(views.html.sectionForm(app, appForm, section, formSection, opp, fields, questions, answers, errs, hints))
              case CostSection => Ok(views.html.costSectionForm(app, appForm, section, formSection, opp, fields, questions, answers, errs, hints))
            }
          case (None, _) => NotFound
        }

      case None => Future.successful(Ok(views.html.wip(routes.ApplicationController.show(id).url)))
    }
  }

  def postSection(id: ApplicationId, sectionNumber: Int) = Action.async(JsonForm.parser) { implicit request =>
    takeAction(id, sectionNumber, request.body.action, request.body.values)
  }

  def takeAction(id: ApplicationId, sectionNumber: Int, button: ButtonAction, fieldValues: JsObject): Future[Result] = {
    button match {
      case Complete => doComplete(id, sectionNumber, fieldValues)
      case Save => doSave(id, sectionNumber, fieldValues)
      case SaveItem => doSaveItem(id, sectionNumber, fieldValues)
      case Preview => doPreview(id, sectionNumber, fieldValues)
    }
  }

  def doSave(id: ApplicationId, sectionNumber: Int, fieldValues: JsObject): Future[Result] =
    applications.saveSection(id, sectionNumber, fieldValues).map { _ =>
      Redirect(routes.ApplicationController.show(id))
    }

  def doComplete(id: ApplicationId, sectionNumber: Int, fieldValues: JsObject): Future[Result] = {
    val errs = check(fieldValues, checksFor(sectionNumber))
    if (errs.isEmpty) {
      applications.completeSection(id, sectionNumber, fieldValues).map { _ =>
        Redirect(routes.ApplicationController.show(id))
      }
    } else redisplaySectionForm(id, sectionNumber, JsonHelpers.flatten("", fieldValues), errs)
  }

  def doPreview(id: ApplicationId, sectionNumber: Int, fieldValues: JsObject): Future[Result] = {
    val errs = check(fieldValues, previewChecksFor(sectionNumber))
    if (errs.isEmpty) {
      applications.saveSection(id, sectionNumber, fieldValues).map { _ =>
        Redirect(routes.ApplicationPreviewController.previewSection(id, sectionNumber))
      }
    } else redisplaySectionForm(id, sectionNumber, JsonHelpers.flatten("", fieldValues), errs)
  }


  def doSaveItem(id: ApplicationId, sectionNumber: Int, fieldValues: JsObject): Future[Result] = {
    // Save if there are no errors, otherwise redisplay with errors
    val errs = check(fieldValues, checksFor(sectionNumber))
    if (errs.isEmpty) {
      doSave(id, sectionNumber, fieldValues)
    } else redisplaySectionForm(id, sectionNumber, JsonHelpers.flatten("", fieldValues), errs)
  }
}
