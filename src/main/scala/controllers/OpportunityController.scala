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

  def showGuidancePage(id: OpportunityId) = Action {
    Ok(views.html.guidance(id))
  }

  def wip(backUrl: String) = Action {
    Ok(views.html.wip(backUrl))
  }

  def showOverviewPage(opportunityId: OpportunityId) = Action.async {
    (for {
      op <- OptionT(opportunities.byId(opportunityId))
      app <- OptionT(applications.byOpportunityId(opportunityId))
    } yield (op, app)
      ).value.map {
        case Some((op, app)) => Ok(views.html.manage.previewOpportunity(op, app) )
        case None => NotFound
      }
  }

  def duplicate(opportunityId: OpportunityId) = Action.async {
      OptionT(opportunities.byId(opportunityId)).value.map {
        case Some(op) => Ok(views.html.wip( controllers.routes.OpportunityController.showOverviewPage(opportunityId).url ) )
        case None => NotFound
    }
  }

}
