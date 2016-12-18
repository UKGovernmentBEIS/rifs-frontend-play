package controllers.manage

import javax.inject.Inject

import actions.OpportunityAction
import controllers.FieldCheckHelpers
import forms.CurrencyField
import forms.validation.{CurrencyValidator, FieldError}
import models._
import play.api.libs.json._
import play.api.mvc._
import play.twirl.api.Html
import services.OpportunityOps

import scala.concurrent.ExecutionContext

class GrantValueController @Inject()(
                                      val opportunities: OpportunityOps,
                                      val OpportunityAction: OpportunityAction)
                                    (implicit val ec: ExecutionContext)
  extends Controller with SummarySave[Option[String], BigDecimal] {

  override val fieldName = "grantValue"
  val field = CurrencyField(None, fieldName, CurrencyValidator.greaterThanZero)
  val questions = Map(
    fieldName -> Question("Maximum amount from this opportunity", None, None)
  )
  override val validator = field.validator

  override implicit def inReads: Reads[Option[String]] = OptionReads[String]

  def view(id: OpportunityId) = OpportunityAction(id) { request =>
    if (request.opportunity.isPublished)
      Ok(views.html.manage.viewGrantValue(request.opportunity))
    else
      Redirect(editPage(id))
  }

  def edit(id: OpportunityId) = OpportunityAction(id) { request =>
    request.opportunity.publishedAt match {
      case Some(_) => BadRequest
      case None =>
        val values = JsObject(Seq(fieldName -> JsNumber(request.opportunity.value.amount)))
        Ok(doEdit(request.opportunity, values, Nil))
    }
  }

  def preview(id: OpportunityId) = OpportunityAction(id) { request =>
    Ok(views.html.manage.viewGrantValue(request.opportunity, request.flash.get(PREVIEW_BACK_URL_FLASH)))
  }

  override def doEdit(opp: Opportunity, values: JsObject, errs: Seq[FieldError]): Html = {
    val hints = FieldCheckHelpers.hinting(values, Map(fieldName -> field.check))
    views.html.manage.editCostSectionForm(field, opp, editPage(opp.id).url, questions, values, errs, hints)
  }

  override def updateSummary(opportunity: Opportunity, v: BigDecimal) =
    opportunity.summary.copy(value = OpportunityValue(v, "per event"))

  override def editPage(id: OpportunityId): Call = controllers.manage.routes.GrantValueController.edit(id)


}
