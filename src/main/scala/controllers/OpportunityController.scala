package controllers

import javax.inject.Inject

import actions.OpportunityAction
import models.OpportunityId
import play.api.mvc.{Action, Controller}
import services.{ApplicationFormOps, OpportunityOps}

import scala.concurrent.ExecutionContext

class OpportunityController @Inject()(opportunities: OpportunityOps, appForms: ApplicationFormOps, OpportunityAction: OpportunityAction)(implicit ec: ExecutionContext) extends Controller {

  def showOpportunities = Action.async {
    opportunities.getOpenOpportunitySummaries.map { os => Ok(views.html.showOpportunities(os)) }
  }

  def showOpportunity(id: OpportunityId, sectionNumber: Option[Int]) = OpportunityAction(id).async { request =>
    appForms.byOpportunityId(id).map {
      case Some(appForm) => Ok(views.html.showOpportunity(appForm.id, request.opportunity, sectionNumber.getOrElse(1)))
      case None => NotFound
    }
  }

  def viewTitle(id: OpportunityId) = OpportunityAction(id) { request =>
    Ok(views.html.manage.viewTitle(request.opportunity))
  }

  def viewDescription(id: OpportunityId) = OpportunityAction(id) { request =>
    Ok(views.html.manage.viewDescription(request.opportunity))
  }

  def viewGrantValue(id: OpportunityId) = OpportunityAction(id) { request =>
    Ok(views.html.manage.viewGrantValue(request.opportunity))
  }

  def viewOppSection(id: OpportunityId, sectionNum: Int) = OpportunityAction(id) { request =>
    Ok(views.html.manage.viewOppSection(request.opportunity, sectionNum))
  }

  def wip(backUrl: String) = Action {
    Ok(views.html.wip(backUrl))
  }

  def duplicate(opportunityId: OpportunityId) = OpportunityAction(opportunityId) { request =>
    Ok(views.html.wip(controllers.routes.OpportunityController.showOverviewPage(opportunityId).url))
  }

  def viewQuestions(id: OpportunityId, sectionNumber: Int) = OpportunityAction(id).async { request =>
    appForms.byOpportunityId(id).map {
      case Some(appForm) =>
        appForm.sections.find(_.sectionNumber == sectionNumber) match {
          case Some(formSection) => Ok(views.html.manage.viewQuestions(request.opportunity, formSection))
          case None => NotFound
        }
      case None => NotFound
    }
  }

  def showGuidancePage(id: OpportunityId) = Action {
    Ok(views.html.guidance(id))
  }

  def showOverviewPage(id: OpportunityId) = OpportunityAction(id).async { request =>
    appForms.byOpportunityId(id).map {
      case Some(appForm) => Ok(views.html.manage.previewOpportunity(request.uri, request.opportunity, appForm))
      case None => NotFound
    }
  }
}



