package controllers

import javax.inject.Inject

import actions.{OppSectionAction, OpportunityAction}
import models.OpportunityId
import play.api.mvc.{Action, Controller}
import services.{ApplicationFormOps, OpportunityOps}

import scala.concurrent.ExecutionContext

class OpportunityController @Inject()(
                                       opportunities: OpportunityOps,
                                       appForms: ApplicationFormOps,
                                       OpportunityAction: OpportunityAction,
                                       OppSectionAction: OppSectionAction
                                     )(implicit ec: ExecutionContext) extends Controller {

  def showOpportunities = Action.async {
    opportunities.getOpenOpportunitySummaries.map { os => Ok(views.html.showOpportunities(os)) }
  }

  def showOpportunity(id: OpportunityId, sectionNumber: Option[Int]) = OpportunityAction(id) { request =>
    Redirect(controllers.routes.OpportunityController.showOpportunitySection(id, sectionNumber.getOrElse(1)))
  }

  def showOpportunitySection(id: OpportunityId, sectionNum: Int) = OppSectionAction(id, sectionNum).async { request =>
    appForms.byOpportunityId(id).map {
      case Some(appForm) => Ok(views.html.showOpportunity(appForm, request.opportunity, request.section))
      case None => NotFound
    }
  }

  def showGuidancePage(id: OpportunityId) = Action {
    Ok(views.html.guidance(id))
  }

  def wip(backUrl: String) = Action {
    Ok(views.html.wip(backUrl))
  }

}



