package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import models.OpportunityId
import play.api.mvc.{Action, Controller}
import services.{ApplicationOps, OpportunityOps}

import scala.concurrent.ExecutionContext

class OpportunityController @Inject()(opportunities: OpportunityOps, applications: ApplicationOps)(implicit ec: ExecutionContext) extends Controller {

  def showOpportunities = Action.async {
    opportunities.getOpenOpportunitySummaries.map { os => Ok(views.html.showOpportunities(os)) }
  }

  def showOpportunity(id: OpportunityId, sectionNumber: Option[Int]) = Action.async {
    val ft = for {
      o <- OptionT(opportunities.byId(id))
      a <- OptionT(applications.byOpportunityId(id))
    } yield (o, a)

    ft.value.map {
      case Some((o, a)) => Ok(views.html.showOpportunity(a.id, o, sectionNumber.getOrElse(1)))
      case None => NotFound
    }
  }

  def showGuidancePage = Action {
    Ok(views.html.guidance())
  }

  def wip = Action { Ok(views.html.wip())}

}
