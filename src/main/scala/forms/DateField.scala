package forms

import cats.data.NonEmptyList
import forms.validation.FieldError
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.twirl.api.Html

case class DateValues(day: Option[String], month: Option[String], year: Option[String])

case class DateField(name: String, rules: Seq[FieldRule] = Seq(), value: Option[DateValues] = None, errs: Option[NonEmptyList[FieldError]] = None, question: Option[String] = None) extends Field {
  override def renderFormInput(questions: Map[String, String], answers: Map[String, String], errs: Seq[FieldError]): Html =
    views.html.renderers.dateField(this, questions, answers, errs)

  override def renderPreview(answers: Map[String, String]): Html = views.html.renderers.preview.dateField(this, answers)

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
}
