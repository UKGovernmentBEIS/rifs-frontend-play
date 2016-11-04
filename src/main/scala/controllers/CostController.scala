package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import models.ApplicationId
import play.api.mvc.{Action, Controller}
import services.ApplicationOps

import scala.concurrent.{ExecutionContext, Future}

class CostController @Inject()(actionHandler: ActionHandler, applications: ApplicationOps)(implicit ec: ExecutionContext)
  extends Controller {

  def addItem(applicationId: ApplicationId, sectionNumber: Int) = Action.async { implicit request =>
    val details1 = actionHandler.gatherApplicationDetails(applicationId)

    val details2 = for {
      ds <- OptionT(details1)
      fs <- OptionT.fromOption[Future](ds._2.sections.find(_.sectionNumber == sectionNumber))
    } yield (ds, fs)

    import ApplicationData._
    val questions = questionsFor(sectionNumber)
    val fields = fieldsFor(sectionNumber).getOrElse(Seq())

    details2.value.map {
      case Some(((overview, form, opp), fs)) => Ok(views.html.costItemForm(overview, form, fs, opp, fields, questions, Map(), List(), List()), None)
      case None => NotFound
    }
  }

  def postItem(applicationId: ApplicationId, sectionNumber: Int, itemNumber: Option[Int]) = Action.async { implicit request =>
  }
}
