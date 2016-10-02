package controllers

import javax.inject.Inject

import models.OpportunityId
import play.api.mvc.{Action, Controller}
import services.OpportunityOps

import scala.concurrent.ExecutionContext

class OpportunityController @Inject()(opportunities: OpportunityOps)(implicit ec: ExecutionContext) extends Controller {

  def showOpportunities = Action.async {
    opportunities.getOpenOpportunitySummaries.map {os => Ok(views.html.showOpportunities(os))}
  }

  def showOpportunity(id: OpportunityId, sectionNumber:Option[Int]) = Action.async {
    opportunities.getOpportunity(id).map {
      case Some(o) => Ok(views.html.showOpportunity(o, sectionNumber.getOrElse(1)))
      case None => NotFound
    }
  }

}
