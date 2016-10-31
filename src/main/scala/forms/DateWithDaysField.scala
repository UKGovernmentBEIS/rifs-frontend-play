package forms

import forms.validation.{DateWithDaysValidator, DateWithDaysValues, FieldError, FieldHint}
import models.Question
import org.joda.time.format._
import play.api.Logger
import play.twirl.api.Html

case class DateWithDaysField(name: String, validator: DateWithDaysValidator) extends Field {

  val dateField = DateField(s"$name.date")
  val daysField = TextField(Some("Days"), s"$name.days", isNumeric = true)

  val fmt = DateTimeFormat.forPattern("d MMMM yyyy")

  override def renderFormInput(questions: Map[String, Question], answers: Map[String, String], errs: Seq[FieldError], hints: Seq[FieldHint]): Html =
    views.html.renderers.dateWithDaysField(this, questions, answers, errs, hints)

  override def renderPreview(answers: Map[String, String]): Html = {
    Logger.debug(answers.toString())
    val day = answers.get(s"${dateField.name}.day")
    val month = answers.get(s"${dateField.name}.month")
    val year = answers.get(s"${dateField.name}.year")

    val vs = DateWithDaysValues(Some(DateValues(day, month, year)), answers.get(s"${daysField.name}"))
    validator.validate("", vs).map { dwd =>
      val endDate = dwd.date.plusDays(dwd.days - 1)
      views.html.renderers.preview.dateWithDaysField(this, fmt.print(dwd.date), dwd.days, fmt.print(endDate))
    }.leftMap { errs =>
      Logger.debug(errs.toString())
      // TODO: we rely on only being called with valid answers, but what if they're not?
      views.html.renderers.preview.dateWithDaysField(this, "", 0, "")
    }.fold(identity, identity)
  }
}
