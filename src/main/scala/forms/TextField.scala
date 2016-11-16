package forms

import controllers.JsonHelpers
import forms.validation.{FieldError, FieldHint}
import models.{ApplicationFormSection, ApplicationOverview, Question}
import play.api.libs.json.JsObject
import play.twirl.api.Html

case class TextField(label: Option[String], name: String, isNumeric: Boolean) extends Field {
  override def renderFormInput(app: ApplicationOverview, formSection: ApplicationFormSection, questions: Map[String, Question], answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html =
    views.html.renderers.textField(this, questions, JsonHelpers.flatten(answers), errs, hints)

  override def renderPreview(app: ApplicationOverview, formSection: ApplicationFormSection, answers: JsObject): Html =
    views.html.renderers.preview.textField(this, JsonHelpers.flatten(answers))
}
