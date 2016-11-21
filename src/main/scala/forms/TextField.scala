package forms

import controllers.JsonHelpers
import forms.validation.{FieldError, FieldHint}
import models.{ApplicationDetail, ApplicationFormSection}
import play.api.libs.json.JsObject
import play.twirl.api.Html

case class TextField(label: Option[String], name: String, isNumeric: Boolean) extends Field {
  override def renderFormInput(app: ApplicationDetail, formSection: ApplicationFormSection, answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html =
    views.html.renderers.textField(this, formSection.questionMap, JsonHelpers.flatten(answers), errs, hints)

  override def renderPreview(app: ApplicationDetail, formSection: ApplicationFormSection, answers: JsObject): Html =
    views.html.renderers.preview.textField(this, JsonHelpers.flatten(answers))
}
