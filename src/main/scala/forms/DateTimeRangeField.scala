package forms

import controllers.JsonHelpers
import forms.validation._
import models.ApplicationSectionDetail
import play.api.libs.json.JsObject
import play.twirl.api.Html

case class DateTimeRangeField(name: String, allowPast: Boolean, isEndDateMandatory: Boolean) extends Field with DateTimeFormats {
  val startDateField = DateField(s"$name.startDate")
  val endDateField = DateField(s"$name.endDate")

  val validator = DateTimeRangeValidator(allowPast = allowPast, isEndDateMandatory = isEndDateMandatory)

  override def renderFormInput(app: ApplicationSectionDetail, answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html =
    views.html.renderers.dateTimeRangeField(this, app, answers, errs, hints)

  override def renderPreview(app: ApplicationSectionDetail, answers: JsObject): Html = {
    val flattenedAnswers = JsonHelpers.flatten("", answers)
    val startDay = flattenedAnswers.get(s"${startDateField.name}.day")
    val startMonth = flattenedAnswers.get(s"${startDateField.name}.month")
    val startYear = flattenedAnswers.get(s"${startDateField.name}.year")

    val endDay = flattenedAnswers.get(s"${endDateField.name}.day")
    val endMonth = flattenedAnswers.get(s"${endDateField.name}.month")
    val endYear = flattenedAnswers.get(s"${endDateField.name}.year")

    val hasClosingDate = flattenedAnswers.get(s"$name.has_closing_date").exists(_.trim == "yes")

    val vs = DateTimeRangeValues(Some(DateValues(startDay, startMonth, startYear)), Some(DateValues(endDay, endMonth, endYear)), endDateProvided = hasClosingDate)

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

}
