package controllers

import javax.inject.Inject

import actions.OpportunityAction
import cats.data.Validated._
import forms.validation.DateTimeRangeValues
import forms.{DateTimeRangeField, DateValues}
import models._
import org.joda.time.LocalDate
import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}
import play.api.mvc.{Action, Controller}
import services.{ApplicationFormOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

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

  def showOpportunityPreview(id: OpportunityId, sectionNumber: Option[Int]) = OpportunityAction(id) { implicit request =>
    Ok(views.html.opportunityPreview(request.opportunity, sectionNumber.getOrElse(1)))
  }

  def showNewOpportunityForm = Action {
    Ok(views.html.newOpportunityChoice())
  }

  def chooseHowToCreateOpportunity(choiceText: Option[String]) = Action { implicit request =>
    CreateOpportunityChoice(choiceText).map {
      case NewOpportunityChoice => Ok(views.html.wip(routes.OpportunityController.showNewOpportunityForm().url))
      case ReuseOpportunityChoice => Redirect(controllers.routes.OpportunityController.showOpportunityLibrary())
    }.getOrElse(Redirect(controllers.routes.OpportunityController.showNewOpportunityForm()))
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

  val deadlinesField = DateTimeRangeField("deadlines", allowPast = false, isEndDateMandatory = false)
  val deadlineQuestions = Map(
    "deadlines.startDate" -> Question("When does the opportunity open?"),
    "deadlines.endDate" -> Question("What is the closing date?")
  )


  implicit val dvFmt = Json.format[DateValues]
  implicit val dtrFmt = Json.format[DateTimeRangeValues]

  def viewDeadlines(id: OpportunityId) = OpportunityAction(id) { request =>
    val answers = JsObject(Seq("deadlines" -> Json.toJson(dateTimeRangeValuesFor(request.opportunity))))
    Ok(views.html.manage.viewDeadlines(deadlinesField, request.opportunity, deadlineQuestions, answers))
  }

  def editDeadlines(id: OpportunityId) = OpportunityAction(id) { request =>
    val answers = JsObject(Seq("deadlines" -> Json.toJson(dateTimeRangeValuesFor(request.opportunity))))
    Ok(views.html.manage.editDeadlinesForm(deadlinesField, request.opportunity, deadlineQuestions, answers, Seq(), Seq()))
  }

  private def dateTimeRangeValuesFor(opp: Opportunity) = {
    val sdv = dateValuesFor(opp.startDate)
    val edv = opp.endDate.map(dateValuesFor)
    DateTimeRangeValues(Some(sdv), edv, edv.map(_ => "yes").orElse(Some("no")))
  }

  private def dateValuesFor(ld: LocalDate) =
    DateValues(Some(ld.getDayOfMonth.toString), Some(ld.getMonthOfYear.toString), Some(ld.getYear.toString))

  def saveDeadlines(id: OpportunityId) = OpportunityAction(id).async(JsonForm.parser) { implicit request =>
    (request.body.values \ "deadlines").validate[DateTimeRangeValues] match {
      case JsSuccess(vs, _) =>
        deadlinesField.validator.validate(deadlinesField.name, vs) match {
          case Valid(v) =>
            val summary = request.opportunity.summary.copy(startDate = v.startDate, endDate = v.endDate)
            opportunities.saveSummary(summary).map(_ => Redirect(controllers.routes.OpportunityController.showOverviewPage(id)))
          case Invalid(errors) =>
            Future.successful(Ok(views.html.manage.editDeadlinesForm(deadlinesField, request.opportunity, deadlineQuestions, request.body.values, errors.toList, Seq())))
        }
      case JsError(errors) => Future.successful(BadRequest(errors.toString))
    }
  }

  def showOpportunitySetupGuidance(id: OpportunityId) = showPMGuidancePage

  def showOverviewPage(opportunityId: OpportunityId) = OpportunityAction(opportunityId).async { request =>
    appForms.byOpportunityId(opportunityId).map {
      case Some(appForm) => Ok(views.html.manage.previewOpportunity(request.opportunity, appForm))
      case None => NotFound
    }
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