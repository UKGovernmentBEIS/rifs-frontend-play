package controllers.manage

import javax.inject.Inject

import actions.OpportunityAction
import controllers.{FieldCheckHelpers, JsonForm, Preview}
import forms.CurrencyField
import forms.validation.CurrencyValidator
import models.{Opportunity, OpportunityId, Question}
import play.api.libs.json.{JsNumber, JsObject}
import play.api.mvc._
import services.OpportunityOps

import scala.concurrent.{ExecutionContext, Future}

class GrantValueController @Inject()(opportunities: OpportunityOps, OpportunityAction: OpportunityAction)(implicit ec: ExecutionContext) extends Controller {
  val grantValueFieldName = "grantValue"
  val grantValueField = CurrencyField(None, grantValueFieldName, Some(CurrencyValidator.greaterThanZero))
  val viewGrantValueFlash = "ViewGrantValueFlash"

  def view(id: OpportunityId) = OpportunityAction(id) { request =>
    request.opportunity.publishedAt match {
      case Some(_) => Ok(views.html.manage.viewGrantValue(request.opportunity))
      case None => Redirect(controllers.manage.routes.GrantValueController.edit(id))
    }
  }

  def edit(id: OpportunityId) = OpportunityAction(id) { request =>
    request.opportunity.publishedAt match {
      case Some(_) => BadRequest
      case None => doEditSection(request.opportunity,
        JsObject(Seq(grantValueFieldName -> JsNumber(request.opportunity.value.amount))),
        Nil
      )
    }
  }

  def doEditSection(opp: Opportunity, initial: JsObject, errs: Seq[forms.validation.FieldError]) = {
    val hints = FieldCheckHelpers.hinting(initial, Map(grantValueFieldName -> grantValueField.check))
    val q = Question("Maximum amount from this opportunity", None, None)

    Ok(views.html.manage.editCostSectionForm(grantValueField, opp,
      routes.GrantValueController.edit(opp.id).url, Map(grantValueFieldName -> q), initial, errs, hints))
  }

  def save(id: OpportunityId) = OpportunityAction(id).async(JsonForm.parser) { implicit request =>
    (request.body.values \ grantValueFieldName).toOption.map { fValue =>
      grantValueField.check(grantValueFieldName, fValue) match {
        case Nil =>
          val summary = request.opportunity.summary
          opportunities.saveSummary(summary.copy(value = summary.value.copy(amount = fValue.as[BigDecimal]))).map { _ =>
            request.body.action match {
              case Preview =>
                Redirect(controllers.manage.routes.GrantValueController.preview(id))
                  .flashing(PREVIEW_BACK_URL_FLASH ->
                    controllers.manage.routes.GrantValueController.edit(id).url)
              case _ =>
                Redirect(controllers.manage.routes.OpportunityController.showOverviewPage(id))
            }
          }
        case errors => Future.successful(doEditSection(request.opportunity, request.body.values, errors))
      }
    }.getOrElse(Future.successful(BadRequest))
  }

  def preview(id: OpportunityId) = OpportunityAction(id) { request =>
    Ok(views.html.manage.viewGrantValue(request.opportunity, request.flash.get(PREVIEW_BACK_URL_FLASH)))
  }

}
