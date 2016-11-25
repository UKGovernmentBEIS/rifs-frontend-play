package forms

import controllers.{FieldCheck, FieldChecks, JsonHelpers}
import forms.validation._
import models.ApplicationSectionDetail
import play.api.libs.json.{JsObject, Json}
import play.twirl.api.Html

case class DateTimeRangeField(name: String, allowPast: Boolean, isEndDateMandatory: Boolean) extends Field with DateTimeFormats {
  implicit val dvReads = Json.reads[DateValues]
  implicit val dtrReads = Json.reads[DateTimeRangeValues]

  val startDateField = DateField(s"$name.startDate", allowPast)
  val endDateField = DateField(s"$name.endDate", allowPast)

  val validator = DateTimeRangeValidator(allowPast = allowPast, isEndDateMandatory = isEndDateMandatory)

  override val check: FieldCheck = FieldChecks.fromValidator(validator)

  override def renderFormInput(app: ApplicationSectionDetail, answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html =
    views.html.renderers.dateTimeRangeField(this, app, answers, errs, hints)

  override def renderPreview(app: ApplicationSectionDetail, answers: JsObject): Html = {
    val flattenedAnswers = JsonHelpers.flatten("", answers)
    val startDateValues = dateValuesFor(s"${startDateField.name}", flattenedAnswers)
    val endDateValues = dateValuesFor(s"${endDateField.name}", flattenedAnswers)
    val vs = DateTimeRangeValues(Some(startDateValues), Some(endDateValues), endDateProvided = None)

    validator.validate("", vs).map { dtr =>
      views.html.renderers.preview.dateTimeRangeField(
        this,
        fmt.print(dtr.startDate),
        accessFmt.print(dtr.startDate),
        dtr.endDate.map(fmt.print),
        dtr.endDate.map(accessFmt.print))
    }.leftMap { errs =>
      views.html.renderers.preview.dateTimeRangeField(this, "None", "None", None, None)
    }.fold(identity, identity)
  }

  def dateValuesFor(name: String, answers: Map[String, String]): DateValues =
    DateValues(answers.get(s"$name.day"), answers.get(s"$name.month"), answers.get(s"$name.year"))

}
