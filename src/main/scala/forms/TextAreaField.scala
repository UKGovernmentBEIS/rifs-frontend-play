package forms

import forms.validation.{FieldError, FieldHint}
import models.Question
import play.twirl.api.Html

case class TextAreaField(label: Option[String], name: String) extends Field {

  override def renderPreview(answers:Map[String, String]): Html = views.html.renderers.preview.textAreaField(this, answers)

  override def renderFormInput(questions: Map[String, Question], answers: Map[String, String], errs: Seq[FieldError], hints: Seq[FieldHint]): Html =
    views.html.renderers.textAreaField(this, questions, answers, errs, hints)
}
