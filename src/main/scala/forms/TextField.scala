package forms

import forms.validation.FieldError
import play.twirl.api.Html

case class TextField(label: Option[String], name: String) extends Field {
  override def renderFormInput(questions: Map[String, String], answers: Map[String, String], errs: Seq[FieldError]): Html =
    views.html.renderers.textField(this, questions, answers, errs)

  override def renderPreview(answers: Map[String, String]): Html = views.html.renderers.preview.textField(this, answers)
}
