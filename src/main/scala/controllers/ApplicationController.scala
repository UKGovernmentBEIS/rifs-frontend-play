package controllers

import javax.inject.Inject

import models._
import play.api.Logger
import play.api.mvc.{Action, Controller}
import services.ApplicationOps

import scala.concurrent.{ExecutionContext, Future}

class ApplicationController @Inject()(actionHandler: ActionHandler, applications: ApplicationOps)(implicit ec: ExecutionContext)
  extends Controller with ApplicationResults {

  def showOrCreateForForm(id: ApplicationFormId) = Action.async {
    applications.getOrCreateForForm(id).map {
      case Some(app) => redirectToOverview(app.id)
      case None => NotFound
    }
  }

  def show(id: ApplicationId) = Action.async {
    actionHandler.gatherApplicationDetails(id).map {
      case Some((overview, form, opp)) => Ok(views.html.showApplicationForm(form, overview, opp))
      case None => NotFound
    }
  }

  def reset = Action.async {
    applications.deleteAll().map(_ => Redirect(controllers.routes.StartPageController.startPage()))
  }

  import ApplicationData._
  import FieldCheckHelpers._

  //TODO: DELETE WHEN SURE WE AREN"T TAKING THIS APPROACH
  //def editSectionForm(id: ApplicationId, sectionNumber: Int) = showSectionForm (id, sectionNumber, true)
  //
  //  def showSectionForm(id: ApplicationId, sectionNumber: Int, forceEditMode: Boolean=false) = Action.async { request =>
  //    fieldsFor(sectionNumber) match {
  //      case Some(fields) =>
  //        applications.getSection(id, sectionNumber).flatMap { section =>
  //          section.flatMap(_.completedAtText) match {
  //            case None =>   //|| (forceEditMode == true)
  //              Logger.debug("***************************************")
  //              Logger.debug(request.body.toString)
  //              val hints = section.map(s => hinting(s.answers, checksFor(sectionNumber))).getOrElse(List())
  //              actionHandler.renderSectionForm(id, sectionNumber, section, questionsFor(sectionNumber), fields, noErrors, hints)
  //            case _  if (forceEditMode == true) =>
  //              val hints = section.map(s => hinting(s.answers, checksFor(sectionNumber))).getOrElse(List())
  //              actionHandler.renderSectionForm(id, sectionNumber, section, questionsFor(sectionNumber), fields, noErrors, hints)
  //            case _  if (forceEditMode == false) =>
  //              actionHandler.renderSectionPreview(id, sectionNumber, section, fields)
  //          }
  //        }
  //      // Temporary hack to display the WIP page for sections that we haven't yet coded up
  //      case None => Future.successful(wip(routes.ApplicationController.show(id).url))
  //    }
  //  }

  //TODO: TIDY a, cut down from previous so could probably be refactored. b, consider merging into showSectionForm method (as per commented out code above)
  def editSectionForm(id: ApplicationId, sectionNumber: Int) = Action.async { request =>
    fieldsFor(sectionNumber) match {
      case Some(fields) => {
        applications.getSection(id, sectionNumber).flatMap { section => {
          val hints = section.map(s => hinting(s.answers, checksFor(sectionNumber))).getOrElse(List())
          actionHandler.renderSectionForm(id, sectionNumber, section, questionsFor(sectionNumber), fields, noErrors, hints)
        }
        }
      }
      //Needs an error page maybe - SECTION DOESN'T EXIST - we shouldn't end up here as it is either edit or add new
      case None => Future.successful(wip(routes.ApplicationController.show(id).url))
    }
  }

  def showSectionForm(id: ApplicationId, sectionNumber: Int) = Action.async { request =>
    fieldsFor(sectionNumber) match {
      case Some(fields) => {
        applications.getSection(id, sectionNumber).flatMap { section =>
          section.flatMap(_.completedAtText) match {
            case None => //|| (forceEditMode == true)
              val hints = section.map(s => hinting(s.answers, checksFor(sectionNumber))).getOrElse(List())
              actionHandler.renderSectionForm(id, sectionNumber, section, questionsFor(sectionNumber), fields, noErrors, hints)
            case _ =>
              actionHandler.displayCompletedPreview(id, sectionNumber)
          }
        }
      }
      // Temporary hack to display the WIP page for sections that we haven't yet coded up
      case None => Future.successful(wip(routes.ApplicationController.show(id).url))
    }
  }

  def postSection(id: ApplicationId, sectionNumber: Int) = Action.async(JsonForm.parser) { implicit request =>
    request.body.action match {
      case Complete => actionHandler.doComplete(id, sectionNumber, request.body.values)
      case Save => actionHandler.doSave(id, sectionNumber, request.body.values)
      case SaveItem => actionHandler.doSaveItem(id, sectionNumber, request.body.values)
      case Preview => actionHandler.doPreview(id, sectionNumber, request.body.values)
    }
  }


}
