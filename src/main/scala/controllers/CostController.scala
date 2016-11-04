package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import controllers.FieldCheckHelpers.FieldErrors
import models.ApplicationId
import play.api.libs.json.JsObject
import play.api.mvc.{Action, Controller, Result}
import services.ApplicationOps

import scala.concurrent.{ExecutionContext, Future}

class CostController @Inject()(actionHandler: ActionHandler, applications: ApplicationOps)(implicit ec: ExecutionContext)
  extends Controller {

  def addItem(applicationId: ApplicationId, sectionNumber: Int) = Action.async { implicit request =>
    showItemForm(applicationId, sectionNumber, Map(), List())
  }

  def postItem(applicationId: ApplicationId, sectionNumber: Int) = Action.async(JsonForm.parser) { implicit request =>
    val fieldValues: JsObject = request.body.values
    applications.saveItem(applicationId, sectionNumber, fieldValues).flatMap {
      case Nil => Future.successful(Redirect(routes.ApplicationController.showSectionForm(applicationId, sectionNumber)))
      case errs => showItemForm(applicationId, sectionNumber, JsonHelpers.flatten("", fieldValues), errs)
    }
  }

  def showItemForm(applicationId: ApplicationId, sectionNumber: Int, answers: Map[String, String], errs: FieldErrors): Future[Result] = {
    val details1 = actionHandler.gatherApplicationDetails(applicationId)

    val details2 = for {
      ds <- OptionT(details1)
      fs <- OptionT.fromOption[Future](ds._2.sections.find(_.sectionNumber == sectionNumber))
    } yield (ds, fs)

    import ApplicationData._
    val questions = questionsFor(sectionNumber)
    val fields = fieldsFor(sectionNumber).getOrElse(Seq())

    details2.value.map {
      case Some(((overview, form, opp), fs)) => Ok(views.html.costItemForm(overview, form, fs, opp, fields, questions, answers, errs, List(), None))
      case None => NotFound
    }
  }


}
