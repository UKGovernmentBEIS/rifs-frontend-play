package forms

import cats.data.NonEmptyList
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue}
import play.twirl.api.Html

case class DateWithDaysField(name: String, dateField: DateField, daysField: TextField) extends Field {
  override def renderFormInput: Html = views.html.renderers.dateWithDaysField(this)

  override def renderPreview: Html = views.html.renderers.preview.dateWithDaysField(this)

  override def rules: Seq[FieldRule] = dateField.rules ++ daysField.rules

  override def derender(fieldValues: JsObject): Option[(String, JsValue)] = {
    val subValues = Seq(dateField, daysField).flatMap(_.derender(fieldValues))
    Some(name -> JsObject(subValues))
  }

  override def withValuesFrom(values: JsObject): DateWithDaysField = {
    Logger.debug(values.toString())
    objectValue(values, name).map { o =>
      copy(dateField = dateField.withValuesFrom(o), daysField = daysField.withValuesFrom(o))
    }.getOrElse(this)
  }

  override def withErrorsFrom(errs: Map[String, NonEmptyList[String]]): DateWithDaysField =
    copy(dateField = dateField.withErrorsFrom(errs), daysField = daysField.withErrorsFrom(errs))

  override def withQuestionsFrom(questions: Map[String, String]): DateWithDaysField =
    copy(dateField = dateField.withQuestionsFrom(questions), daysField = daysField.withQuestionsFrom(questions))
}
