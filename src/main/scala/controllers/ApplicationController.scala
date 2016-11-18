package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import forms.validation.SectionError
import models._
import org.joda.time.format.DateTimeFormat
import org.joda.time.LocalDateTime


import play.api.mvc.{Action, Controller}
import services.{ApplicationFormOps, ApplicationOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

class ApplicationController @Inject()(actionHandler: ActionHandler, applications: ApplicationOps, forms: ApplicationFormOps, opps: OpportunityOps)(implicit ec: ExecutionContext)
  extends Controller with ApplicationResults {

  def showOrCreateForForm(id: ApplicationFormId) = Action.async {
    applications.getOrCreateForForm(id).map {
      case Some(app) => redirectToOverview(app.id)
      case None => NotFound
    }
  }

  def show(id: ApplicationId) = Action.async {
    gatherApplicationDetails(id).map {
      case Some((overview, form, opp)) => Ok(views.html.showApplicationForm(form, overview, opp, List.empty))
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
      case Some((app, appForm, appFormSection, opp)) =>
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
      case Some((app, appForm, appFormSection, opp)) =>
        applications.getSection(id, sectionNumber).flatMap { section =>
          section.flatMap(_.completedAtText) match {
            case None =>
              val hints = section.map(s => hinting(s.answers, checksFor(sectionNumber))).getOrElse(List.empty)
              actionHandler.renderSectionForm(id, sectionNumber, section, appFormSection.questionMap, noErrors, hints)
            case _ =>
              Future.successful(actionHandler.redirectToPreview(id, sectionNumber))
          }
        }
      case None => Future(NotFound)
    }
  }

  def postSection(id: ApplicationId, sectionNumber: Int) = Action.async(JsonForm.parser) { implicit request =>
    request.body.action match {
      case Complete => actionHandler.doComplete(id, sectionNumber, request.body.values)
      case Save => actionHandler.doSave(id, sectionNumber, request.body.values)
      case SaveItem => actionHandler.doSaveItem(id, sectionNumber, request.body.values)
      case Preview => actionHandler.doPreview(id, sectionNumber, request.body.values)
      case completeAndPreview => actionHandler.completeAndPreview(id, sectionNumber, request.body.values)
    }
  }

  def submit(id: ApplicationId) = Action.async { request =>
    gatherApplicationDetails(id).flatMap {
      case Some((overview, form, opp)) =>
        val sectionErrors: Seq[SectionError] = form.sections.sortBy(_.sectionNumber).flatMap { fs =>
          overview.sections.find(_.sectionNumber == fs.sectionNumber) match {
            case None => Some(SectionError(fs, "Not started"))
            case Some(s) => checkSection(fs, s)
          }
        }
        if(sectionErrors.isEmpty){
          val emailto = "experiencederic@university.ac.uk"
          val dtf = DateTimeFormat.forPattern("HH:mm:ss")
          val appsubmittime = dtf.print(LocalDateTime.now()) //returns TimeZOne Europe/London
          actionHandler.doSubmit(id).map {
            case Some(e) =>
              Ok(views.html.submitApplicationForm(e.applicationRef, emailto, appsubmittime))
            case None => NotFound
          }
        }
        else
          Future.successful( Ok(views.html.showApplicationForm(form, overview, opp, sectionErrors)) )

      case None => Future.successful( NotFound )
    }
  }

  def checkSection(appFormSection: ApplicationFormSection, appSection: ApplicationSectionOverview): Option[SectionError] = {
    appSection.completedAt match {
      case Some(_) => None
      case None => Some(SectionError(appFormSection, "In progress"))
    }
  }

  def gatherApplicationDetails(id: ApplicationId): Future[Option[(ApplicationOverview, ApplicationForm, Opportunity)]] = {
    for {
      a <- OptionT(applications.overview(id))
      af <- OptionT(forms.byId(a.applicationFormId))
      o <- OptionT(opps.byId(af.opportunityId))
    } yield (a, af, o)
  }.value

}
