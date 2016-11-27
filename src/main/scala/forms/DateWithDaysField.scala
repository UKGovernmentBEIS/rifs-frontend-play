package forms

import controllers.{FieldCheck, FieldChecks, JsonHelpers}
import forms.validation._
import models._
import play.api.libs.json.{JsObject, Json}
import play.twirl.api.Html

case class DateWithDaysField(name: String, allowPast: Boolean, minValue: Int, maxValue: Int) extends Field with DateTimeFormats {
  implicit val dvReads = Json.reads[DateValues]
  implicit val dtrReads = Json.reads[DateWithDaysValues]

  val dateField = DateField(s"$name.date", allowPast)
  val daysField = TextField(Some("Days"), s"$name.days", isNumeric = true, 1)

  val validator = DateWithDaysValidator(allowPast, minValue, maxValue)

  override val check: FieldCheck = FieldChecks.fromValidator(validator)

  override def renderFormInput(questions: Map[String, Question], answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]) =
    views.html.renderers.dateWithDaysField(this, questions, answers, errs, hints)

  override def renderPreview(questions: Map[String, Question], answers: JsObject) = {
    val flattenedAnswers = JsonHelpers.flatten("", answers)
    val day = flattenedAnswers.get(s"${dateField.name}.day")
    val month = flattenedAnswers.get(s"${dateField.name}.month")
    val year = flattenedAnswers.get(s"${dateField.name}.year")

    val vs = DateWithDaysValues(Some(DateValues(day, month, year)), flattenedAnswers.get(s"${daysField.name}"))
    validator.validate("", vs).map { dwd =>
      val endDate = dwd.date.plusDays(dwd.days - 1)
      views.html.renderers.preview.dateWithDaysField(this, fmt.print(dwd.date), dwd.days, fmt.print(endDate), accessFmt.print(dwd.date), accessFmt.print(endDate))
    }.leftMap { errs =>
      views.html.renderers.preview.dateWithDaysField(this, "", 0, "", "", "")
    }.fold(identity, identity)
  }
}