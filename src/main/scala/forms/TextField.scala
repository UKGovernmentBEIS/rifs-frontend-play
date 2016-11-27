package forms

import controllers.{FieldCheck, FieldChecks, JsonHelpers}
import forms.validation.{FieldError, FieldHint}
import models.{ApplicationSectionDetail, Question}
import play.api.libs.json.JsObject
import play.twirl.api.Html

case class TextField(label: Option[String], name: String, isNumeric: Boolean, maxWords: Int) extends Field {
  override val check: FieldCheck = FieldChecks.mandatoryText(maxWords)

  override val previewCheck: FieldCheck = FieldChecks.mandatoryCheck

  override def renderFormInput(questions: Map[String, Question], answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]) =
    views.html.renderers.textField(this, questions, JsonHelpers.flatten(answers), errs, hints)

  override def renderPreview(questions: Map[String, Question], answers: JsObject) =
    views.html.renderers.preview.textField(this, JsonHelpers.flatten(answers))

}
