package controllers

import javax.inject.Inject

import forms.validation.SectionError
import models._
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.JsObject
import play.api.mvc.{Action, Controller}
import services.{ApplicationFormOps, ApplicationOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

class ApplicationController @Inject()(
                                       actionHandler: ActionHandler,
                                       applications: ApplicationOps,
                                       forms: ApplicationFormOps,
                                       opps: OpportunityOps)(implicit ec: ExecutionContext)
  extends Controller with ApplicationResults {

  def showOrCreateForForm(id: ApplicationFormId) = Action.async {
    applications.getOrCreateForForm(id).map {
      case Some(app) => redirectToOverview(app.id)
      case None => NotFound
    }
  }

  def show(id: ApplicationId) = Action.async {
    gatherApplicationDetails(id).map {
      case Some(app) => Ok(views.html.showApplicationForm(app, List.empty))
      case None => NotFound
    }
  }

  def reset = Action.async {
    applications.deleteAll().map(_ => Redirect(controllers.routes.StartPageController.startPage()))
  }

  import ApplicationData._
  import FieldCheckHelpers._

  def editSectionForm(id: ApplicationId, sectionNumber: Int) = Action.async { request =>
    actionHandler.gatherSectionDetails(id, sectionNumber).flatMap {
      case Some((app, appFormSection)) =>
        applications.getSection(id, sectionNumber).flatMap { section =>
          val hints = section.map(s => hinting(s.answers, checksFor(sectionNumber))).getOrElse(List.empty)
          actionHandler.renderSectionForm(id, sectionNumber, section, appFormSection.questionMap, noErrors, hints)
        }
      case None => Future(NotFound)
    }
  }

  def resetAndEditSection(id: ApplicationId, sectionNumber: Int) = Action.async { request =>
    applications.clearSectionCompletedDate(id, sectionNumber).map { _ =>
      Redirect(controllers.routes.ApplicationController.editSectionForm(id, sectionNumber))
    }
  }

  def showSectionForm(id: ApplicationId, sectionNumber: Int) = Action.async { request =>
    actionHandler.gatherSectionDetails(id, sectionNumber).flatMap {
      case Some((app, appFormSection)) =>
        app.sections.find(_.sectionNumber == sectionNumber) match {
          case None =>
            val hints = hinting(JsObject(List.empty), checksFor(sectionNumber))
            actionHandler.renderSectionForm(id, sectionNumber, None, appFormSection.questionMap, noErrors, hints)

          case Some(s) =>
            if (s.isComplete) Future.successful(actionHandler.redirectToPreview(id, sectionNumber))
            else {
              val hints = hinting(s.answers, checksFor(sectionNumber))
              actionHandler.renderSectionForm(id, sectionNumber, Some(s), appFormSection.questionMap, noErrors, hints)
            }
        }
      case None => Future(NotFound)
    }
  }

  def postSection(id: ApplicationId, sectionNumber: Int) = Action.async(JsonForm.parser) {
    implicit request =>
      request.body.action match {
        case Complete => actionHandler.doComplete(id, sectionNumber, request.body.values)
        case Save => actionHandler.doSave(id, sectionNumber, request.body.values)
        case SaveItem => actionHandler.doSaveItem(id, sectionNumber, request.body.values)
        case Preview => actionHandler.doPreview(id, sectionNumber, request.body.values)
        case completeAndPreview => actionHandler.completeAndPreview(id, sectionNumber, request.body.values)
      }
  }

  def submit(id: ApplicationId) = Action.async {
    request =>
      gatherApplicationDetails(id).flatMap {
        case Some(app) =>
          val sectionErrors: Seq[SectionError] = app.applicationForm.sections.sortBy(_.sectionNumber).flatMap { fs =>
            app.sections.find(_.sectionNumber == fs.sectionNumber) match {
              case None => Some(SectionError(fs, "Not started"))
              case Some(s) => checkSection(fs, s)
            }
          }

          if (sectionErrors.isEmpty) {
            val emailto = "experiencederic@university.ac.uk"
            val dtf = DateTimeFormat.forPattern("HH:mm:ss")
            val appsubmittime = dtf.print(LocalDateTime.now()) //returns TimeZOne Europe/London
            actionHandler.doSubmit(id).map {
              case Some(e) =>
                Ok(views.html.submitApplicationForm(e.applicationRef, emailto, appsubmittime))
              case None => NotFound
            }
          } else Future.successful(Ok(views.html.showApplicationForm(app, sectionErrors)))

        case None => Future.successful(NotFound)
      }
  }

  def checkSection(appFormSection: ApplicationFormSection, appSection: ApplicationSection): Option[SectionError] = {
    appSection.completedAt match {
      case Some(_) => None
      case None => Some(SectionError(appFormSection, "In progress"))
    }
  }

  def gatherApplicationDetails(id: ApplicationId): Future[Option[ApplicationDetail]] = applications.detail(id)

}
