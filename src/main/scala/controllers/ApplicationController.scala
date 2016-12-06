package controllers

import javax.inject.Inject

import actions.{AppDetailAction, AppSectionAction}
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
                                       opps: OpportunityOps,
                                       AppDetailAction: AppDetailAction,
                                       AppSectionAction: AppSectionAction
                                     )(implicit ec: ExecutionContext)
  extends Controller with ApplicationResults {

  def showOrCreateForForm(id: ApplicationFormId) = Action.async {
    applications.getOrCreateForForm(id).map {
      case Some(app) => redirectToOverview(app.id)
      case None => NotFound
    }
  }

  def show(id: ApplicationId) = AppDetailAction(id) { request =>
    Ok(views.html.showApplicationForm(request.appDetail, List.empty))
  }

  def reset = Action.async {
    applications.reset().map(_ => Redirect(controllers.routes.StartPageController.startPage()))
  }

  import FieldCheckHelpers._

  def editSectionForm(id: ApplicationId, sectionNumber: Int) = AppSectionAction(id, sectionNumber) { request =>
    val hints = request.appSection.section.map(s => hinting(s.answers, checksFor(request.appSection.formSection))).getOrElse(List.empty)
    actionHandler.renderSectionForm(request.appSection, noErrors, hints)
  }

  def resetAndEditSection(id: ApplicationId, sectionNumber: Int) = Action.async { request =>
    applications.clearSectionCompletedDate(id, sectionNumber).map { _ =>
      Redirect(controllers.routes.ApplicationController.editSectionForm(id, sectionNumber))
    }
  }

  def showSectionForm(id: ApplicationId, sectionNumber: Int) = AppSectionAction(id, sectionNumber) { request =>
    request.appSection.section match {
      case None =>
        val hints = hinting(JsObject(List.empty), checksFor(request.appSection.formSection))
        actionHandler.renderSectionForm(request.appSection, noErrors, hints)

      case Some(s) =>
        if (s.isComplete) actionHandler.redirectToPreview(id, sectionNumber)
        else {
          val hints = hinting(s.answers, checksFor(request.appSection.formSection))
          actionHandler.renderSectionForm(request.appSection, noErrors, hints)
        }
    }
  }

  def postSection(id: ApplicationId, sectionNumber: Int) = AppSectionAction(id, sectionNumber).async(JsonForm.parser) {
    implicit request =>
      request.body.action match {
        case Complete => actionHandler.doComplete(request.appSection, request.body.values)
        case Save => actionHandler.doSave(request.appSection, request.body.values)
        case SaveItem => actionHandler.doSaveItem(request.appSection, request.body.values)
        case Preview => actionHandler.doPreview(request.appSection, request.body.values)
        case completeAndPreview => actionHandler.completeAndPreview(request.appSection, request.body.values)
      }
  }

  def submit(id: ApplicationId) = AppDetailAction(id).async { request =>
    val sectionErrors: Seq[SectionError] = request.appDetail.applicationForm.sections.sortBy(_.sectionNumber).flatMap { fs =>
      request.appDetail.sections.find(_.sectionNumber == fs.sectionNumber) match {
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
    } else Future.successful(Ok(views.html.showApplicationForm(request.appDetail, sectionErrors)))
  }

  def checkSection(appFormSection: ApplicationFormSection, appSection: ApplicationSection): Option[SectionError] = {
    appSection.completedAt match {
      case Some(_) => None
      case None => Some(SectionError(appFormSection, "In progress"))
    }
  }

  def checksFor(formSection: ApplicationFormSection): Map[String, FieldCheck] =
    formSection.fields.map(f => f.name -> f.check).toMap

}
