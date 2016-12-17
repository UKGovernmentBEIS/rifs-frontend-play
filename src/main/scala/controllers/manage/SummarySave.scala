package controllers.manage

import controllers.{ButtonAction, Preview}
import forms.validation.FieldError
import models.{Opportunity, OpportunityId, OpportunitySummary}
import play.api.libs.json.JsObject
import play.api.mvc.{Call, Controller}
import play.twirl.api.Html
import services.OpportunityOps

import scala.concurrent.ExecutionContext

trait SummarySave {
  self: Controller =>
  implicit def ec: ExecutionContext

  def opportunities: OpportunityOps

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

  def previewPage(id: OpportunityId) = controllers.manage.routes.GrantValueController.preview(id)

  def overviewPage(id: OpportunityId) = controllers.manage.routes.OpportunityController.showOverviewPage(id)

  def editPage(id: OpportunityId): Call

  def doEdit(opp: Opportunity, values: JsObject, errs: Seq[FieldError]): Html
}
