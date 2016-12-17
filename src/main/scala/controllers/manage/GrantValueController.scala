package controllers.manage

import javax.inject.Inject

import actions.OpportunityAction
import cats.data.Validated.{Invalid, Valid}
import controllers.{ButtonAction, FieldCheckHelpers, JsonForm, Preview}
import forms.CurrencyField
import forms.validation.CurrencyValidator
import models._
import play.api.libs.json._
import play.api.mvc._
import services.OpportunityOps

import scala.concurrent.{ExecutionContext, Future}

class GrantValueController @Inject()(opportunities: OpportunityOps, OpportunityAction: OpportunityAction)(implicit ec: ExecutionContext) extends Controller {
  val grantValueFieldName = "grantValue"
  val grantValueField = CurrencyField(None, grantValueFieldName, CurrencyValidator.greaterThanZero)
  val viewGrantValueFlash = "ViewGrantValueFlash"

  def view(id: OpportunityId) = OpportunityAction(id) { request =>
    if (request.opportunity.isPublished)
      Ok(views.html.manage.viewGrantValue(request.opportunity))
    else
      Redirect(editPage(id))
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

  implicit def OptionReads[T: Reads] = new Reads[Option[T]] {
    override def reads(json: JsValue): JsResult[Option[T]] = json match {
      case JsNull => JsSuccess(None)
      case j => j.validate[T].map(Some(_))
    }
  }

  def save(id: OpportunityId) = OpportunityAction(id).async(JsonForm.parser) { implicit request =>
    val opportunity = request.opportunity
    val action = request.body.action
    val validator = grantValueField.validator
    val fieldName = grantValueField.name

    (request.body.values \ fieldName).validate[Option[String]] match {
      case JsError(errors) => Future.successful(BadRequest(errors.toString))
      case JsSuccess(vs, _) =>
        validator.validate(fieldName, vs) match {
          case Valid(v) =>
            saveSummary(id, action, updateSummary(opportunity, v))
          case Invalid(errors) =>
            Future.successful(doEditSection(opportunity, request.body.values, errors.toList))
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

  private def updateSummary(opportunity: Opportunity, v: BigDecimal) =
    opportunity.summary.copy(value = OpportunityValue(v, "per event"))

  private def previewPage(id: OpportunityId) =
    controllers.manage.routes.GrantValueController.preview(id)


  private def overviewPage(id: OpportunityId) =
    controllers.manage.routes.OpportunityController.showOverviewPage(id)


  private def editPage(id: OpportunityId) =
    controllers.manage.routes.GrantValueController.edit(id)

  def preview(id: OpportunityId) = OpportunityAction(id) { request =>
    Ok(views.html.manage.viewGrantValue(request.opportunity, request.flash.get(PREVIEW_BACK_URL_FLASH)))
  }

}
