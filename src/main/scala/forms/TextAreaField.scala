package forms

import cats.data.NonEmptyList
import play.api.libs.json.JsObject
import play.twirl.api.Html

case class TextAreaField(label: Option[String], name: String, rules: Seq[FieldRule], value: Option[String] = None, errs: Option[NonEmptyList[String]] = None, question: Option[String] = None) extends Field {
  override def withValuesFrom(values: JsObject): TextAreaField = this.copy(value = stringValue(values, name))

  override def withErrorsFrom(errs: Map[String, NonEmptyList[String]]): Field = this.copy(errs = errs.get(name))

  override def renderFormInput: Html = views.html.renderers.textAreaField(this)

  override def withQuestionsFrom(questions: Map[String, String]): TextAreaField = this.copy(question = questions.get(name))
}
