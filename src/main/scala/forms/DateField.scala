package forms

import cats.data.NonEmptyList
import forms.validation.FieldError
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.twirl.api.Html

case class DateValues(day: Option[String], month: Option[String], year: Option[String])

case class DateField(name: String, rules: Seq[FieldRule]= Seq(), value: Option[DateValues] = None, errs: Option[NonEmptyList[FieldError]] = None, question: Option[String] = None) extends Field {
  override def renderFormInput: Html = views.html.renderers.dateField(this)

  override def renderPreview: Html = views.html.renderers.preview.dateField(this)

  override def withValuesFrom(values: JsObject): DateField = {
    val value = objectValue(values, name).map { o =>
      val day = stringValue(o, "day")
      val month = stringValue(o, "month")
      val year = stringValue(o, "year")
      DateValues(day, month, year)
    }
    this.copy(value = value)
  }

  val dayName = s"${name}__day"
  val monthName = s"${name}__month"
  val yearName = s"${name}__year"

  override def derender(fieldValues: JsObject): Option[(String, JsValue)] = {
    Some(name -> JsObject(Seq(
      "day" -> (fieldValues \ dayName).validate[JsString].asOpt.getOrElse(JsString("")),
      "month" -> (fieldValues \ monthName).validate[JsString].asOpt.getOrElse(JsString("")),
      "year" -> (fieldValues \ yearName).validate[JsString].asOpt.getOrElse(JsString(""))
    )))
  }

  override def withErrorsFrom(errs: Map[String, NonEmptyList[FieldError]]): DateField = this.copy(errs = errs.get(name))

  override def withQuestionsFrom(questions: Map[String, String]): DateField = this.copy(question = questions.get(name))


}
