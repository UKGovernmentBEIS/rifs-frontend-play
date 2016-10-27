package forms

import cats.data.NonEmptyList
import play.api.libs.json.JsObject
import play.twirl.api.Html

case class TextField(label: Option[String], name: String, isNumeric: Boolean, rules: Seq[FieldRule], value: Option[String] = None, errs: Option[NonEmptyList[String]] = None, question: Option[String] = None) extends Field {
  override def renderFormInput: Html = views.html.renderers.textField(this)

  override def renderPreview: Html = views.html.renderers.preview.textField(this)

  override def withValuesFrom(values: JsObject): TextField = this.copy(value = stringValue(values, name))

  override def withErrorsFrom(errs: Map[String, NonEmptyList[String]]): Field = this.copy(errs = errs.get(name))

  override def withQuestionsFrom(questions: Map[String, String]): TextField = this.copy(question = questions.get(name))
}
