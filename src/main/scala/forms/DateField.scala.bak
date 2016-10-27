package forms

import cats.data.NonEmptyList
import models.Question
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.twirl.api.Html

case class DateValues(day: String, month: String, year: String)

case class DateField(name: String, rules: Seq[FieldRule], value: Option[DateValues] = None, errs: Option[NonEmptyList[String]] = None, question: Option[Question] = None) extends Field {
  override def renderFormInput: Html = views.html.renderers.dateField(this)

  override def renderPreview: Html = views.html.renderers.preview.dateField(this)

  override def withValuesFrom(values: JsObject): DateField = {
    val value = objectValue(values, name).map { o =>
      val day = stringValue(o, "day").getOrElse("")
      val month = stringValue(o, "month").getOrElse("")
      val year = stringValue(o, "year").getOrElse("")
      DateValues(day, month, year)
    }
    this.copy(value = value)
  }

  val dayName = s"${name}__day"
  val monthName = s"${name}__month"
  val yearName = s"${name}__year"

  override def derender(fieldValues: JsObject): Seq[(String, JsValue)] = {
    Seq("date" -> JsObject(Seq(
      "day" -> (fieldValues \ dayName).validate[JsString].asOpt.getOrElse(JsString("")),
      "month" -> (fieldValues \ monthName).validate[JsString].asOpt.getOrElse(JsString("")),
      "year" -> (fieldValues \ yearName).validate[JsString].asOpt.getOrElse(JsString(""))
    )))
  }

  override def withErrorsFrom(errs: Map[String, NonEmptyList[String]]): DateField = this.copy(errs = errs.get(name))

  override def withQuestionsFrom(questions: Map[String, Question]): DateField = this.copy(question = questions.get(name))


}
