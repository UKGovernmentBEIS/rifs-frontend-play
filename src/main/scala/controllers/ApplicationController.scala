package controllers

import javax.inject.Inject

import models.{ApplicationId, OpportunityId}
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

class ApplicationController @Inject()()(implicit ec: ExecutionContext) extends Controller {

  def show(id: ApplicationId) = Action {
    Ok(views.html.showApplication(OpportunityId(1)))
  }

}
