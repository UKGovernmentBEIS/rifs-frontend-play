package forms

import forms.validation.{FieldError, FieldHint}
import play.twirl.api.Html

case class DateWithDaysField(name: String, dateField: DateField, daysField: TextField) extends Field {
  override def renderFormInput(questions: Map[String, String], answers: Map[String, String], errs: Seq[FieldError], hints:Seq[FieldHint]): Html =
    views.html.renderers.dateWithDaysField(this,questions, answers, errs, hints)

  override def renderPreview(answers: Map[String, String]): Html = views.html.renderers.preview.dateWithDaysField(this, answers)
}
