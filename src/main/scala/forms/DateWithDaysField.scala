package forms

import controllers.JsonHelpers
import forms.validation.{DateWithDaysValidator, DateWithDaysValues, FieldError, FieldHint}
import models.{ApplicationFormSection, ApplicationOverview, Question}
import org.joda.time.format._
import org.joda.time.LocalDate
import play.api.Logger
import play.api.libs.json.JsObject
import play.twirl.api.Html

case class DateWithDaysField(name: String, validator: DateWithDaysValidator) extends Field {

  val dateField = DateField(s"$name.date", validator.dateValidator)
  val daysField = TextField(Some("Days"), s"$name.days", isNumeric = true)

  val fmt = DateTimeFormat.forPattern("d MMMM yyyy")
  val accessFmt = AccessibleDateTimeFormat()

  override def renderFormInput(app: ApplicationOverview, formSection: ApplicationFormSection, questions: Map[String, Question], answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html =
    views.html.renderers.dateWithDaysField(this, app, formSection, questions, answers, errs, hints)

  override def renderPreview(app: ApplicationOverview, formSection: ApplicationFormSection, answers: JsObject): Html = {
    val flattenedAnswers = JsonHelpers.flatten("", answers)
    val day = flattenedAnswers.get(s"${dateField.name}.day")
    val month = flattenedAnswers.get(s"${dateField.name}.month")
    val year = flattenedAnswers.get(s"${dateField.name}.year")

    val vs = DateWithDaysValues(Some(DateValues(day, month, year)), flattenedAnswers.get(s"${daysField.name}"))
    validator.validate("", vs).map { dwd =>
      val endDate = dwd.date.plusDays(dwd.days - 1)
      views.html.renderers.preview.dateWithDaysField(this, fmt.print(dwd.date), dwd.days, fmt.print(endDate), accessFmt.print(dwd.date), accessFmt.print(endDate))
    }.leftMap { errs =>
      // TODO: we rely on only being called with valid answers, but what if they're not?
      views.html.renderers.preview.dateWithDaysField(this, "", 0, "", "", "")
    }.fold(identity, identity)
  }
}