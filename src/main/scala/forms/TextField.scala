package forms

import controllers.{FieldCheck, FieldChecks, JsonHelpers}
import forms.validation.{FieldError, FieldHint, MandatoryValidator, WordCountValidator}
import models.Question
import play.api.libs.json.JsObject
import controllers.manage._

case class TextField(label: Option[String], name: String, isNumeric: Boolean, maxWords: Int) extends Field {
  val validator = MandatoryValidator(label).andThen(WordCountValidator(maxWords))

  override val check: FieldCheck = FieldChecks.fromValidator(validator)

  override val previewCheck: FieldCheck = FieldChecks.mandatoryCheck

  override def renderFormInput(questions: Map[String, Question], answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]) =
    views.html.renderers.textField(this, questions, JsonHelpers.flatten(answers), errs, hints)

  override def renderPreview(questions: Map[String, Question], answers: JsObject) =
    views.html.renderers.preview.textField(this, JsonHelpers.flatten(answers))

}
