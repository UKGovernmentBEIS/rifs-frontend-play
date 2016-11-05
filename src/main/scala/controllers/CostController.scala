package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import controllers.FieldCheckHelpers.FieldErrors
import forms.validation.CostItemValues
import models.ApplicationId
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller, Result}
import services.ApplicationOps

import scala.concurrent.{ExecutionContext, Future}

class CostController @Inject()(actionHandler: ActionHandler, applications: ApplicationOps)(implicit ec: ExecutionContext)
  extends Controller with ApplicationResults {

  implicit val costItemValuesR = Json.reads[CostItemValues]

  def addItem(applicationId: ApplicationId, sectionNumber: Int) = Action.async { implicit request =>
    showItemForm(applicationId, sectionNumber, Map(), List())
  }

  def saveItem(applicationId: ApplicationId, sectionNumber: Int) = Action.async(JsonForm.parser) { implicit request =>
    applications.saveItem(applicationId, sectionNumber, request.body.values).flatMap {
      case Nil => Future.successful(redirectToSectionForm(applicationId, sectionNumber))
      case errs => showItemForm(applicationId, sectionNumber, JsonHelpers.flatten("", request.body.values), errs)
    }
  }

  def deleteItem(applicationId: ApplicationId, sectionNumber: Int, itemNumber: Int) = Action.async {
    applications.deleteItem(applicationId, sectionNumber, itemNumber).map { _ =>
      redirectToSectionForm(applicationId, sectionNumber)
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
    val cancelLink = sectionFormCall(applicationId, sectionNumber)

    details2.value.map {
      case Some(((overview, form, opp), fs)) => Ok(views.html.costItemForm(overview, form, fs, opp, fields, questions, answers, errs, List(), cancelLink, None))
      case None => NotFound
    }
  }
}
