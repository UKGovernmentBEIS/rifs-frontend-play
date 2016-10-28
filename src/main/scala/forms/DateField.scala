package forms

import cats.data.NonEmptyList
import forms.validation.FieldError
import play.api.Logger
import play.twirl.api.Html

case class DateValues(day: Option[String], month: Option[String], year: Option[String])

case class DateField(name: String) extends Field {
  override def renderFormInput(questions: Map[String, String], answers: Map[String, String], errs: Seq[FieldError]): Html = {
    Logger.debug(s"rendering date field with $name using $answers and $errs")
    views.html.renderers.dateField(this, questions, answers, errs)
  }

  override def renderPreview(answers: Map[String, String]): Html = views.html.renderers.preview.dateField(this, answers)
}
