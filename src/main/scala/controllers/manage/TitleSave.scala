package controllers.manage

import actions.OpportunityAction
import cats.data.Validated.{Invalid, Valid}
import controllers.JsonForm
import forms.TextField
import forms.validation.{FieldError, FieldHint}
import models.{Opportunity, OpportunityId, Question}
import play.api.libs.json.{JsError, JsObject, JsSuccess}
import play.api.mvc.{Call, Controller}
import play.twirl.api.Html

import scala.concurrent.Future

trait TitleSave extends SummarySave {
  self: Controller =>

  def OpportunityAction: OpportunityAction

  val fieldName = "title"
  val field = TextField(label = Some(fieldName), name = fieldName, isNumeric = false, maxWords = 20)
  val questions = Map(fieldName -> Question("What is your opportunity called ?"))

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

  def updateSummary(opportunity: Opportunity, v: String) = opportunity.summary.copy(title = v)

  override def editPage(id: OpportunityId): Call =
    controllers.manage.routes.TitleController.edit(id)

  override def doEdit(opp: Opportunity, values: JsObject, errs: Seq[FieldError]) :Html= {
    val hints: Seq[FieldHint] = Nil
    views.html.manage.editTitleForm(field, opp, questions, values, errs, hints, "")
  }
}