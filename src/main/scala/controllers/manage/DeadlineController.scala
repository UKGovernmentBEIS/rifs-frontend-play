package controllers.manage

import javax.inject.Inject

import actions.OpportunityAction
import cats.data.Validated.{Invalid, Valid}
import controllers.{JsonForm, Preview}
import forms.validation.DateTimeRangeValues
import forms.{DateTimeRangeField, DateValues}
import models.{Opportunity, OpportunityId, Question}
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


  def view(id: OpportunityId) = OpportunityAction(id) {
    request =>
      val answers = JsObject(Seq(deadlinesFieldName -> Json.toJson(dateTimeRangeValuesFor(request.opportunity))))
      request.opportunity.publishedAt match {
        case Some(dateval) => Ok(views.html.manage.viewDeadlines(deadlinesField, request.opportunity, deadlineQuestions, answers))
        case None => Redirect(controllers.manage.routes.DeadlineController.edit(id))
      }
  }


  def edit(id: OpportunityId) = OpportunityAction(id) { request =>
      val answers = JsObject(Seq(deadlinesFieldName -> Json.toJson(dateTimeRangeValuesFor(request.opportunity))))
      Ok(views.html.manage.editDeadlinesForm(deadlinesField, request.opportunity, deadlineQuestions, answers, Seq(), Seq()))
  }

  def save(id: OpportunityId) = OpportunityAction(id).async(JsonForm.parser) {
    implicit request =>
      (request.body.values \ deadlinesFieldName).validate[DateTimeRangeValues] match {
        case JsSuccess(vs, _) =>
          deadlinesField.validator.validate(deadlinesField.name, vs) match {
            case Valid(v) =>
              val summary = request.opportunity.summary.copy(startDate = v.startDate, endDate = v.endDate)
              opportunities.saveSummary(summary).map {
                _ =>
                  request.body.action match {
                    case Preview =>
                      Redirect(controllers.manage.routes.DeadlineController.preview(id))
                        .flashing(PREVIEW_BACK_URL_FLASH -> controllers.manage.routes.DeadlineController.edit(id).url)
                    case _ =>
                      Redirect(controllers.manage.routes.OpportunityController.showOverviewPage(id))
                  }
              }
            case Invalid(errors) =>
              Future.successful(Ok(views.html.manage.editDeadlinesForm(deadlinesField, request.opportunity, deadlineQuestions, request.body.values, errors.toList, Seq())))
          }
        case JsError(errors) => Future.successful(BadRequest(errors.toString))
      }
  }

  def preview(id: OpportunityId) = OpportunityAction(id) {
    request =>
      val answers = JsObject(Seq(deadlinesFieldName -> Json.toJson(dateTimeRangeValuesFor(request.opportunity))))
      Ok(views.html.manage.previewDeadlines(deadlinesField, request.opportunity, deadlineQuestions, answers, request.flash.get(PREVIEW_BACK_URL_FLASH)))
  }
}
