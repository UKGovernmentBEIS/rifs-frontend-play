package controllers

import javax.inject.Inject

import models.OpportunityId
import play.api.mvc.{Action, Controller}
import services.ApplicationOps

import scala.concurrent.ExecutionContext

class ApplicationController @Inject()(applications: ApplicationOps)(implicit ec: ExecutionContext) extends Controller {

  def show(id: OpportunityId) = Action.async {
    applications.byOpportunityId(id).map {
      case Some(application) => Ok(views.html.showApplication(application))
      case None => NotFound
    }
  }
}
