package forms

import forms.validation.{FieldError, FieldHint, DateFieldValidator}
import models.Question
import org.joda.time.format._
import org.joda.time.LocalDate
import play.api.Logger
import play.twirl.api.Html

case class DateValues(day: Option[String], month: Option[String], year: Option[String])

case class DateField(name: String, validator: DateFieldValidator) extends Field {

  override def renderFormInput(questions: Map[String, Question], answers: Map[String, String], errs: Seq[FieldError], hints:Seq[FieldHint]): Html = {
    Logger.debug(s"rendering date field with $name using $answers and $errs")
    views.html.renderers.dateField(this, questions, answers, errs)
  }

  val fmt = DateTimeFormat.forPattern("d MMMM yyyy")
  val accessFmt = AccessibleDateTimeFormat()

  override def renderPreview(answers: Map[String, String]): Html = {
    val day = answers.get(s"$name.day")
    val month = answers.get(s"$name.month")
    val year = answers.get(s"$name.year")
    validator.validate("", DateValues(day,month,year)).map { date =>
      views.html.renderers.preview.dateField(fmt.print(date), accessFmt.print(date))
    }.leftMap { errs =>
      Logger.debug(errs.toString())
      // TODO: we rely on only being called with valid answers, but what if they're not?
      views.html.renderers.preview.dateField("", "")
    }.fold(identity, identity)
  }
}

case class AccessibleDateTimeFormat() {
  val inner = DateTimeFormat.forPattern("MMMM yyyy");

  def print(date: LocalDate): String = {
    val x = (date.getDayOfMonth) match {
      case n if Seq(11, 12, 13) contains n => n+"th"
      case n if n%10 == 1 => n+"st"
      case n if n%10 == 2 => n+"nd"
      case n if n%10 == 3 => n+"rd"
      case n => n+"th"
    }
    s"$x of ${inner.print(date)}"
  }
}

