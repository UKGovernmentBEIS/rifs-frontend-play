package controllers

import javax.inject.Inject

import models.{Application, ApplicationId, ApplicationSection, OpportunityId}
import play.api.mvc.{Action, Controller}
import services.OpportunityOps

import scala.concurrent.ExecutionContext

class ApplicationController @Inject()(opportunities:OpportunityOps)(implicit ec: ExecutionContext) extends Controller {

  def show(id: OpportunityId) = Action.async {
    opportunities.getApplicationForOpportunity(id).map {
      case Some(application) => Ok(views.html.showApplication(application))
      case None => NotFound
    }


  }

}
