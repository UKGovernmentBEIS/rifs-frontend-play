package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import forms.validation.SectionError
import models._
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
      case Some((overview, form, opp)) => Ok(views.html.showApplicationForm(form, overview, opp, List()))
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
      case Some((app, af, afs, opp)) =>
        fieldsFor(sectionNumber) match {
          case Some(fields) =>
            applications.getSection(id, sectionNumber).flatMap { section =>
              val hints = section.map(s => hinting(s.answers, checksFor(sectionNumber))).getOrElse(List())
              actionHandler.renderSectionForm(id, sectionNumber, section, afs.questionMap, fields, noErrors, hints)
            }
          case None => Future(NotFound)
        }
      case None => Future(NotFound)
    }
  }

  def resetAndEditSection(id: ApplicationId, sectionNumber: Int) = Action.async { request =>
    fieldsFor(sectionNumber) match {
      case Some(fields) =>
        applications.clearSectionCompletedDate(id, sectionNumber).map { _ =>
          Redirect(controllers.routes.ApplicationController.editSectionForm(id, sectionNumber))
        }
      case None => Future(NotFound)
    }
  }

  def showSectionForm(id: ApplicationId, sectionNumber: Int) = Action.async { request =>
    actionHandler.gatherSectionDetails(id, sectionNumber).flatMap {
      case Some((overview, form, afs, opp)) =>
        fieldsFor(sectionNumber) match {
          case Some(fields) =>
            applications.getSection(id, sectionNumber).flatMap { section =>
              section.flatMap(_.completedAtText) match {
                case None =>
                  val hints = section.map(s => hinting(s.answers, checksFor(sectionNumber))).getOrElse(List())
                  actionHandler.renderSectionForm(id, sectionNumber, section, afs.questionMap, fields, noErrors, hints)
                case _ =>
                  Future.successful(actionHandler.redirectToPreview(id, sectionNumber))

              }
            }
          case None => Future(NotFound)
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
          actionHandler.doSubmit(id).map {
            case Some((e)) =>
              Ok(views.html.submitApplicationForm(e.applicationRef, emailto))
            case None => NotFound
          }
        }
        else
          Future.successful( Ok(views.html.showApplicationForm(form, overview, opp, sectionErrors)) )

      case None => Future.successful( NotFound )
    }
  }

  def checkSection(fs: ApplicationFormSection, s: ApplicationSectionOverview): Option[SectionError] = {
    s.completedAt match {
      case Some(_) => None
      case None => Some(SectionError(fs, "In progress"))
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
