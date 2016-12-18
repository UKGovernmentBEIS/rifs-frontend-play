package controllers.manage

import actions.OpportunityAction
import cats.data.Validated.{Invalid, Valid}
import controllers.{ButtonAction, JsonForm, Preview}
import forms.validation.{FieldError, FieldValidator}
import models.{Opportunity, OpportunityId, OpportunitySummary}
import play.api.libs.json.{JsError, JsObject, JsSuccess, Reads}
import play.api.mvc.{Call, Controller}
import play.twirl.api.Html
import services.OpportunityOps

import scala.concurrent.{ExecutionContext, Future}

trait SummarySave[TIn, TOut] {
  self: Controller =>

  implicit def inReads : Reads[TIn]

  implicit def ec: ExecutionContext

  def opportunities: OpportunityOps

  def OpportunityAction: OpportunityAction

  def fieldName: String

  def validator: FieldValidator[TIn, TOut]

  def save(id: OpportunityId) = OpportunityAction(id).async(JsonForm.parser) { implicit request =>
    val opportunity = request.opportunity
    val action = request.body.action

    (request.body.values \ fieldName).validate[TIn] match {
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

  def saveSummary(id: OpportunityId, action: ButtonAction, summary: OpportunitySummary) = {
    opportunities.saveSummary(summary).map { _ =>
      action match {
        case Preview =>
          Redirect(previewPage(id)).flashing(PREVIEW_BACK_URL_FLASH -> editPage(id).url)
        case _ =>
          Redirect(overviewPage(id))
      }
    }
  }

  def updateSummary(opportunity: Opportunity, v: TOut): OpportunitySummary

  def previewPage(id: OpportunityId) = controllers.manage.routes.GrantValueController.preview(id)

  def overviewPage(id: OpportunityId) = controllers.manage.routes.OpportunityController.showOverviewPage(id)

  def editPage(id: OpportunityId): Call

  def doEdit(opp: Opportunity, values: JsObject, errs: Seq[FieldError]): Html
}
