package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import models.ApplicationId
import play.api.mvc.{Action, Controller}
import services.{ApplicationOps, OpportunityOps}

import scala.concurrent.ExecutionContext

class ApplicationController @Inject()(applications: ApplicationOps, opportunities: OpportunityOps)(implicit ec: ExecutionContext) extends Controller {

  def show(id: ApplicationId) = Action.async {
    applications.byId(id).map {
      case Some(application) => Ok(views.html.showApplication(application))
      case None => NotFound
    }
  }

  def title(id: ApplicationId) = Action.async {
    val ft = for {
      a <- OptionT(applications.byId(id))
      o <- OptionT(opportunities.byId(a.opportunityId))
    } yield (a, o)

    ft.value.map {
      case Some((app, opp)) => Ok(views.html.titleForm(app.id, opp))
      case None => NotFound
    }
  }
}
