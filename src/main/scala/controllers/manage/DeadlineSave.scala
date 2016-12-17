package controllers.manage

import actions.OpportunityAction
import cats.data.Validated.{Invalid, Valid}
import controllers.JsonForm
import forms.DateTimeRangeField
import forms.validation.{DateTimeRange, DateTimeRangeValues, FieldError, FieldHint}
import models.{Opportunity, OpportunityId, Question}
import play.api.libs.json.{JsError, JsObject, JsSuccess}
import play.api.mvc.{Call, Controller}
import play.twirl.api.Html

import scala.concurrent.Future

trait DeadlineSave extends SummarySave {
  self: Controller =>

  def OpportunityAction: OpportunityAction

  val fieldName = "deadlines"
  val field = DateTimeRangeField(fieldName, allowPast = false, isEndDateMandatory = false)
  val questions = Map(
    s"$fieldName.startDate" -> Question("When does the opportunity open?"),
    s"$fieldName.endDate" -> Question("What is the closing date?")
  )

  def save(id: OpportunityId) = OpportunityAction(id).async(JsonForm.parser) { implicit request =>
    val opportunity = request.opportunity
    val action = request.body.action
    val validator = field.validator

    (request.body.values \ fieldName).validate[DateTimeRangeValues] match {
      case JsError(errors) => Future.successful(BadRequest(errors.toString))
      case JsSuccess(vs, _) =>
        validator.validate(fieldName, vs) match {
          case Valid(v) =>
            saveSummary(id, action, updateSummary(opportunity, v))
          case Invalid(errors) =>
            Future.successful(Ok(doEdit(opportunity, request.body.values, errors.toList)))
        }
    }
  }

  override def doEdit(opportunity: Opportunity, values: JsObject, errors: Seq[FieldError]): Html = {
    val hints: Seq[FieldHint] = Nil
    views.html.manage.editDeadlinesForm(field, opportunity, questions, values, errors.toList, hints)
  }

  def updateSummary(opportunity: Opportunity, v: DateTimeRange) =
    opportunity.summary.copy(startDate = v.startDate, endDate = v.endDate)

  override def editPage(id: OpportunityId): Call =
    controllers.manage.routes.DeadlineController.edit(id)

}
