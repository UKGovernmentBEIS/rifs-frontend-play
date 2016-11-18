package forms

import controllers.JsonHelpers
import forms.validation.{DateFieldValidator, FieldError, FieldHint}
import models.{ApplicationFormSection, ApplicationOverview, Question}
import org.joda.time.format._
import org.joda.time.LocalDate
import play.api.libs.json.JsObject
import play.twirl.api.Html

case class DateValues(day: Option[String], month: Option[String], year: Option[String])

case class DateField(name: String, validator: DateFieldValidator) extends Field {

  override def renderFormInput(app: ApplicationOverview, formSection: ApplicationFormSection, questions: Map[String, Question], answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html = {
    views.html.renderers.dateField(this, questions, JsonHelpers.flatten(answers), errs)
  }

  val fmt = DateTimeFormat.forPattern("d MMMM yyyy")
  val accessFmt = AccessibleDateTimeFormat()

  override def renderPreview(app: ApplicationOverview, formSection: ApplicationFormSection, answers: JsObject): Html = {
      views.html.renderers.preview.dateField(this, JsonHelpers.flatten(answers))
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

