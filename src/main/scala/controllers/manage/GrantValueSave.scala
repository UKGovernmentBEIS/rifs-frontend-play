package controllers.manage

import actions.OpportunityAction
import cats.data.Validated.{Invalid, Valid}
import controllers.{FieldCheckHelpers, JsonForm}
import forms.CurrencyField
import forms.validation.{CurrencyValidator, FieldError}
import models._
import play.api.libs.json.{JsError, JsObject, JsSuccess}
import play.api.mvc.{Call, Controller}
import play.twirl.api.Html

import scala.concurrent.Future

trait GrantValueSave extends SummarySave {
  self: Controller =>

  def OpportunityAction: OpportunityAction

  val fieldName = "grantValue"
  val field = CurrencyField(None, fieldName, CurrencyValidator.greaterThanZero)
  val questions = Map(
    fieldName -> Question("Maximum amount from this opportunity", None, None)
  )

  def save(id: OpportunityId) = OpportunityAction(id).async(JsonForm.parser) { implicit request =>
    val opportunity = request.opportunity
    val action = request.body.action
    val validator = field.validator

    (request.body.values \ fieldName).validate[Option[String]] match {
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

  override def doEdit(opp: Opportunity, values: JsObject, errs: Seq[FieldError]): Html = {
    val hints = FieldCheckHelpers.hinting(values, Map(fieldName -> field.check))
    views.html.manage.editCostSectionForm(field, opp, editPage(opp.id).url, questions, values, errs, hints)
  }

  private def updateSummary(opportunity: Opportunity, v: BigDecimal) =
    opportunity.summary.copy(value = OpportunityValue(v, "per event"))

  override def editPage(id: OpportunityId): Call = controllers.manage.routes.GrantValueController.edit(id)

}
