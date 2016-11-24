package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import models.OpportunityId
import play.api.mvc.{Action, Controller}
import services.{ApplicationFormOps, OpportunityOps}

import scala.concurrent.ExecutionContext

class OpportunityController @Inject()(opportunities: OpportunityOps, applications: ApplicationFormOps)(implicit ec: ExecutionContext) extends Controller {
  def showOpportunities = Action.async {
    opportunities.getOpenOpportunitySummaries.map { os => Ok(views.html.showOpportunities(os)) }
  }

  def showOpportunity(id: OpportunityId, sectionNumber: Option[Int]) = Action.async {
    // Make the `Future` calls outside the `for` comprehension to allow them to run
    // concurrently. Could use `Cartesian` to give applicative behaviour (`(f1 |@| f2)`) but
    // IntelliJ doesn't handle it well at the moment.
    val f1 = OptionT(opportunities.byId(id))
    val f2 = OptionT(applications.byOpportunityId(id))

    (for (o <- f1; a <- f2) yield (o, a)).value.map {
      case Some((o, a)) => Ok(views.html.showOpportunity(a.id, o, sectionNumber.getOrElse(1)))
      case None => NotFound
    }
  }

  def showNewOpportunityForm = Action {
    Ok(views.html.newOpportunityChoice())
  }

  def chooseHowToCreateOpportunity(choiceText: Option[String]) = Action { implicit request =>
    CreateOpportunityChoice(choiceText).map {
      case NewOpportunityChoice => Ok(views.html.wip(routes.OpportunityController.showNewOpportunityForm().url))
      case ReuseOpportunityChoice => Redirect(controllers.routes.OpportunityController.showOpportunityLibrary)
    }.getOrElse(Redirect(controllers.routes.OpportunityController.showNewOpportunityForm()))
  }

  def showOpportunityLibrary = Action.async {
    opportunities.getOpenOpportunitySummaries.map { os => Ok(views.html.showOpportunityLibrary(os)) }
  }

  def showGuidancePage(id: OpportunityId) = Action {
    Ok(views.html.guidance(id))
  }

  def showPMGuidancePage = Action {
    Ok(views.html.manage.guidance())
  }

  def wip(backUrl: String) = Action {
    Ok(views.html.wip(backUrl))
  }
}

sealed trait CreateOpportunityChoice {
  def name: String
}

object CreateOpportunityChoice {
  def apply(s: Option[String]): Option[CreateOpportunityChoice] = s match {
    case Some(NewOpportunityChoice.name) => Some(NewOpportunityChoice)
    case Some(ReuseOpportunityChoice.name) => Some(ReuseOpportunityChoice)
    case _ => None
  }
}

case object NewOpportunityChoice extends CreateOpportunityChoice {
  val name = "new"
}

case object ReuseOpportunityChoice extends CreateOpportunityChoice {
  val name = "reuse"
}