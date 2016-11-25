package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import models.{ApplicationId, OpportunityId}
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

  def showOpportunityPreview(id: OpportunityId, sectionNumber: Option[Int]) = Action.async {
    val f1 = OptionT(opportunities.byId(id))
    val f2 = OptionT(applications.byOpportunityId(id))

    (for (o <- f1; a <- f2) yield (o, a)).value.map {
      case Some((o, a)) => Ok(views.html.opportunityPreview(a.id, o, sectionNumber.getOrElse(1)))
      case None => NotFound
    }
  }

  def showNewOpportunityForm = Action {
    Ok(views.html.newOpportunityChoice())
  }

  def chooseHowToCreateOpportunity(choiceText: Option[String]) = Action { implicit request =>
    CreateOpportunityChoice(choiceText).map {
      case NewOpportunityChoice => Ok(views.html.wip(routes.OpportunityController.showNewOpportunityForm().url))
      case ReuseOpportunityChoice => Redirect(controllers.routes.OpportunityController.showOpportunityLibrary)
    }.getOrElse(Redirect(controllers.routes.OpportunityController.showNewOpportunityForm()))
  }

  def viewTitle(id:OpportunityId) = Action.async { request =>
    val fopp = opportunities.byId(id)
    fopp.map {
      case Some(opp) =>
        Ok(views.html.manage.viewTitle(opp))
      case None => NotFound
    }
  }


  def viewDeadlines(id:OpportunityId) = Action.async { request =>
    val fopp = opportunities.byId(id)
    fopp.map {
      case Some(opp) =>
        Ok(views.html.manage.viewDeadlines(opp))
      case None => NotFound
    }
  }


  def viewDescription(id:OpportunityId) = Action.async { request =>
    val fopp = opportunities.byId(id)
    fopp.map {
      case Some(opp) =>
        Ok(views.html.manage.viewDescription(opp))
      case None => NotFound
    }
  }

  def viewGrantValue(id:OpportunityId) = Action.async { request =>
    val fopp = opportunities.byId(id)
    fopp.map {
      case Some(opp) =>
        Ok(views.html.manage.viewGrantValue(opp))
      case None => NotFound
    }
  }

  def viewOppSection(id:OpportunityId, oppSection:Int) = Action.async { request =>
    val fopp = opportunities.byId(id)
    fopp.map {
      case Some(opp) =>
        Ok(views.html.manage.viewOppSection(opp, oppSection))
      case None => NotFound
    }
  }

  def showOpportunityLibrary = Action.async {
    opportunities.getOpenOpportunitySummaries.map { os => Ok(views.html.showOpportunityLibrary(os)) }
  }

  def showGuidancePage(id: OpportunityId) = Action {
    Ok(views.html.guidance(id))
  }

  def showPMGuidancePage = Action {
    Ok(views.html.manage.guidance())
  }

  def wip(backUrl: String) = Action {
    Ok(views.html.wip(backUrl))
  }

  def showOpportunitySetupGuidance(id: OpportunityId) = showPMGuidancePage

  def showOverviewPage(opportunityId: OpportunityId) = Action.async {
    val appId = ApplicationId(1)  //we take 1st Application as sample

    (for {
      op <- OptionT(opportunities.byId(opportunityId))
      app <- OptionT(applications.byOpportunityId(opportunityId))
    } yield (op, app)
      ).value.map {
        case Some((op, app)) => Ok(views.html.manage.previewOpportunity(appId, op, app) )
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

sealed trait CreateOpportunityChoice {
  def name: String
}

object CreateOpportunityChoice {
  def apply(s: Option[String]): Option[CreateOpportunityChoice] = s match {
    case Some(NewOpportunityChoice.name) => Some(NewOpportunityChoice)
    case Some(ReuseOpportunityChoice.name) => Some(ReuseOpportunityChoice)
    case _ => None
  }
}

case object NewOpportunityChoice extends CreateOpportunityChoice {
  val name = "new"
}

case object ReuseOpportunityChoice extends CreateOpportunityChoice {
  val name = "reuse"
}