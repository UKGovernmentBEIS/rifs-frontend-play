package controllers.manage

import javax.inject.Inject

import actions.OpportunityAction
import cats.data.Validated.{Invalid, Valid}
import controllers.{ButtonAction, JsonForm, Preview}
import forms.validation.{DateTimeRange, DateTimeRangeValues}
import forms.{DateTimeRangeField, DateValues}
import models.{Opportunity, OpportunityId, OpportunitySummary, Question}
import org.joda.time.LocalDate
import play.api.libs.json._
import play.api.mvc._
import services.OpportunityOps

import scala.concurrent.{ExecutionContext, Future}

class DeadlineController @Inject()(opportunities: OpportunityOps, OpportunityAction: OpportunityAction)(implicit ec: ExecutionContext) extends Controller {
  val deadlinesFieldName = "deadlines"
  val deadlinesField = DateTimeRangeField("deadlines", allowPast = false, isEndDateMandatory = false)
  val deadlineQuestions = Map(
    "deadlines.startDate" -> Question("When does the opportunity open?"),
    "deadlines.endDate" -> Question("What is the closing date?")
  )

  private def dateTimeRangeValuesFor(opp: Opportunity) = {
    val sdv = dateValuesFor(opp.startDate)
    val edv = opp.endDate.map(dateValuesFor)
    DateTimeRangeValues(Some(sdv), edv, edv.map(_ => "yes").orElse(Some("no")))
  }

  private def dateValuesFor(ld: LocalDate) =
    DateValues(Some(ld.getDayOfMonth.toString), Some(ld.getMonthOfYear.toString), Some(ld.getYear.toString))


  def view(id: OpportunityId) = OpportunityAction(id) { request =>
    val answers = JsObject(Seq(deadlinesFieldName -> Json.toJson(dateTimeRangeValuesFor(request.opportunity))))
    if (request.opportunity.isPublished)
      Ok(views.html.manage.viewDeadlines(deadlinesField, request.opportunity, deadlineQuestions, answers))
    else
      Redirect(editPage(id))
  }


  def edit(id: OpportunityId) = OpportunityAction(id) { request =>
    val answers = JsObject(Seq(deadlinesFieldName -> Json.toJson(dateTimeRangeValuesFor(request.opportunity))))
    Ok(views.html.manage.editDeadlinesForm(deadlinesField, request.opportunity, deadlineQuestions, answers, Seq(), Seq()))
  }

  def save(id: OpportunityId) = OpportunityAction(id).async(JsonForm.parser) { implicit request =>
    val opportunity = request.opportunity
    val action = request.body.action
    val validator = deadlinesField.validator
    val fieldName = deadlinesField.name

    (request.body.values \ fieldName).validate[DateTimeRangeValues] match {
      case JsError(errors) => Future.successful(BadRequest(errors.toString))
      case JsSuccess(vs, _) =>
        validator.validate(fieldName, vs) match {
          case Valid(v) =>
            saveSummary(id, action, updateSummary(opportunity, v))
          case Invalid(errors) =>
            Future.successful(Ok(views.html.manage.editDeadlinesForm(deadlinesField, opportunity, deadlineQuestions, request.body.values, errors.toList, Seq())))
        }
    }
  }

  private def saveSummary(id: OpportunityId, action: ButtonAction, summary: OpportunitySummary) = {
    opportunities.saveSummary(summary).map { _ =>
      action match {
        case Preview =>
          Redirect(previewPage(id)).flashing(PREVIEW_BACK_URL_FLASH -> editPage(id).url)
        case _ =>
          Redirect(overviewPage(id))
      }
    }
  }

  private def updateSummary(opportunity: Opportunity, v: DateTimeRange) =
    opportunity.summary.copy(startDate = v.startDate, endDate = v.endDate)

  private def previewPage(id: OpportunityId) =
    controllers.manage.routes.DeadlineController.preview(id)

  private def overviewPage(id: OpportunityId) =
    controllers.manage.routes.OpportunityController.showOverviewPage(id)


  private def editPage(id: OpportunityId) =
    controllers.manage.routes.DeadlineController.edit(id)


  def preview(id: OpportunityId) = OpportunityAction(id) {
    request =>
      val answers = JsObject(Seq(deadlinesFieldName -> Json.toJson(dateTimeRangeValuesFor(request.opportunity))))
      Ok(views.html.manage.previewDeadlines(deadlinesField, request.opportunity, deadlineQuestions, answers, request.flash.get(PREVIEW_BACK_URL_FLASH)))
  }
}
