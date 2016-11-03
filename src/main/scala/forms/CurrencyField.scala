package forms

import forms.validation.{FieldError, FieldHint}
import models.Question
import play.twirl.api.Html

case class CurrencyField(label: Option[String], name: String) extends Field {
  override def renderFormInput(questions: Map[String, Question], answers: Map[String, String], errs: Seq[FieldError], hints: Seq[FieldHint]): Html =
    views.html.renderers.currencyField(this, questions, answers, errs, hints)

  override def renderPreview(answers: Map[String, String]): Html =
    views.html.renderers.preview.currencyField(this, answers)
}
