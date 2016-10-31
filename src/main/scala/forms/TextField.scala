package forms

import forms.validation.{FieldError, FieldHint}
import models.Question
import play.twirl.api.Html

case class TextField(label: Option[String], name: String, isNumeric: Boolean) extends Field {
  override def renderFormInput(questions: Map[String, Question], answers: Map[String, String], errs: Seq[FieldError], hints:Seq[FieldHint]): Html =
    views.html.renderers.textField(this, questions, answers, errs, hints)

  override def renderPreview(answers: Map[String, String]): Html = views.html.renderers.preview.textField(this, answers)
}
