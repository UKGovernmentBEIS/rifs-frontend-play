package forms

import cats.data.NonEmptyList
import models.Question
import play.api.libs.json.JsObject
import play.twirl.api.Html

case class TextAreaField(label: Option[String], name: String, rules: Seq[FieldRule], value: Option[String] = None, errs: Option[NonEmptyList[String]] = None, question: Option[Question] = None) extends Field {

  override def renderPreview: Html = views.html.renderers.preview.textAreaField(this)

  override def renderFormInput: Html = views.html.renderers.textAreaField(this)

  override def withValuesFrom(values: JsObject): TextAreaField = this.copy(value = stringValue(values, name))

  override def withErrorsFrom(errs: Map[String, NonEmptyList[String]]): TextAreaField = this.copy(errs = errs.get(name))

  override def withQuestionsFrom(questions: Map[String, Question]): TextAreaField = this.copy(question = questions.get(name))
}
