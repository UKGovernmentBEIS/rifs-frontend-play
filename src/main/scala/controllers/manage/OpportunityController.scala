package controllers.manage

import javax.inject.Inject

import actions.OpportunityAction
import cats.data.Validated._
import controllers.FieldCheckHelpers.hinting
import controllers._
import forms._
import forms.validation.{CurrencyValidator, DateTimeRangeValues, FieldError}
import models._
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.mvc.{Action, Controller}
import services.{ApplicationFormOps, OpportunityOps}

import scala.concurrent.ExecutionContext

case class OpportunityLibraryEntry(id: OpportunityId, title: String, status: String, structure: String)

class OpportunityController @Inject()(opportunities: OpportunityOps, appForms: ApplicationFormOps, OpportunityAction: OpportunityAction)(implicit ec: ExecutionContext) extends Controller {

  def showOpportunityPreview(id: OpportunityId, sectionNumber: Option[Int]) = OpportunityAction(id).async { implicit request =>
    appForms.byOpportunityId(id).map {
      case Some(appForm) => Ok(views.html.manage.previewDefaultOpportunity(request.uri, request.opportunity, sectionNumber.getOrElse(1), appForm))
      case None => NotFound
    }
  }

  def showOpportunityPublishPreview(id: OpportunityId, sectionNumber: Option[Int]) = OpportunityAction(id).async { implicit request =>
    appForms.byOpportunityId(id).map {
      case Some(appForm) => Ok(views.html.manage.previewPublishOpportunity(request.uri, request.opportunity, sectionNumber.getOrElse(1), appForm))
      case None => NotFound
    }
  }

  def previewOpportunity(id: OpportunityId, sectionNumber: Option[Int]) = OpportunityAction(id).async { implicit request =>
    appForms.byOpportunityId(id).map {
      case Some(appForm) => Ok(views.html.manage.previewDefaultOpportunity(request.uri, request.opportunity, sectionNumber.getOrElse(1), appForm))
      case None => NotFound
    }
  }

  def showNewOpportunityForm() = Action { request =>
    Ok(views.html.manage.newOpportunityChoice(request.uri))
  }

  def chooseHowToCreateOpportunity(choiceText: Option[String]) = Action { implicit request =>
    CreateOpportunityChoice(choiceText).map {
      case NewOpportunityChoice => Ok(views.html.wip(controllers.manage.routes.OpportunityController.showOpportunityLibrary().url))
      case ReuseOpportunityChoice => Redirect(controllers.manage.routes.OpportunityController.showOpportunityLibrary())
    }.getOrElse(Redirect(controllers.manage.routes.OpportunityController.showNewOpportunityForm()))
  }

  private def libraryEntry(o: Opportunity): OpportunityLibraryEntry = OpportunityLibraryEntry(o.id, o.title, o.statusString, "Responsive, claim, FEC")

  def showOpportunityLibrary = Action.async { request =>
    opportunities.getOpportunitySummaries.map { os => Ok(views.html.manage.showOpportunityLibrary(request.uri, os.map(libraryEntry))) }
  }

  def showOverviewPage(id: OpportunityId) = OpportunityAction(id).async { request =>
    appForms.byOpportunityId(id).map {
      case Some(appForm) => Ok(views.html.manage.opportunityOverview(List(), request.uri, request.opportunity, appForm))
      case None => NotFound
    }
  }

  def viewQuestions(id: OpportunityId, sectionNumber: Int) = OpportunityAction(id).async { request =>
    appForms.byOpportunityId(id).map {
      case Some(appForm) =>
        appForm.sections.find(_.sectionNumber == sectionNumber) match {
          case Some(formSection) => Ok(views.html.manage.viewQuestions(request.uri, request.opportunity, formSection))
          case None => NotFound
        }
      case None => NotFound
    }
  }

  def duplicate(id: OpportunityId) = Action.async {
    request =>
      opportunities.duplicate(id).map {
        case Some(newOppId) => Redirect(controllers.manage.routes.OpportunityController.showOverviewPage(newOppId))
        case None => NotFound
      }
  }

  def publish(id: OpportunityId) = OpportunityAction(id).async {
    request =>
      val oppdate = request.opportunity.startDate

      val valueError : Option[FieldError] =
        if (request.opportunity.value.amount > 2000)
          Some(FieldError("", "Maximum grant value is over £2000. Please review "))
        else None

      val dateError:Option[FieldError] =
        if (oppdate.isBefore(LocalDate.now()))
          Some(FieldError("","Opportunity start date is incorrect. Please review"))
        else None

      val errs: Seq[FieldError] = (valueError ++ dateError).toSeq

      if(request.opportunity.value.amount <=2000 && oppdate.isAfter(LocalDate.now())){
        val emailto = "Portfolio.Manager@rifs.gov.uk"
        val dtf = DateTimeFormat.forPattern("HH:mm:ss")
        opportunities.publish(id).map {
          case Some(dt) =>
            Ok(views.html.manage.publishedOpportunity(request.opportunity.id, emailto, dtf.print(dt)))
          case None => NotFound
        }
      } else {
        appForms.byOpportunityId(id).map {
          case Some(appForm) => Ok(views.html.manage.opportunityOverview(errs, request.uri, request.opportunity, appForm))
          case None => NotFound
        }
      }
  }

  def showPMGuidancePage(backUrl: String) = Action {
    request =>
      Ok(views.html.manage.guidance(backUrl))
  }
}
