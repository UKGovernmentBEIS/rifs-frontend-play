package controllers

import javax.inject.Inject

import models.OpportunityId
import play.api.mvc.{Action, Controller}
import services.OpportunityOps

import scala.concurrent.ExecutionContext

class OpportunityController @Inject()(opportunities: OpportunityOps)(implicit ec: ExecutionContext) extends Controller {

  def showOpportunities = Action {
    Ok(views.html.showOpportunities())
  }

  def showOpportunity(id: OpportunityId) = Action.async {
    opportunities.getOpportunity(id).map {
      case Some(o) => Ok(views.html.showOpportunity(o, 1))
      case None => NotFound
    }
  }

}
