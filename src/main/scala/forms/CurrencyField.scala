package forms

import controllers.JsonHelpers
import forms.validation.{FieldError, FieldHint}
import models.{ApplicationDetail, ApplicationFormSection, ApplicationOverview, Question}
import play.api.libs.json.JsObject
import play.twirl.api.Html

case class CurrencyField(label: Option[String], name: String) extends Field {
  override def renderFormInput(app: ApplicationDetail, formSection: ApplicationFormSection, answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html =
    views.html.renderers.currencyField(this, formSection.questionMap, JsonHelpers.flatten(answers), errs, hints)

  override def renderPreview(app: ApplicationDetail, formSection: ApplicationFormSection, answers: JsObject): Html =
    views.html.renderers.preview.currencyField(this, JsonHelpers.flatten(answers))
}
