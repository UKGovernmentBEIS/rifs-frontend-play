package forms.validation

import cats.data.ValidatedNel
import forms.DateValues
import org.joda.time.LocalDate
import play.api.Logger

case class DateWithDaysValues(date: Option[DateValues], days: Option[String])

case class DateWithDays(date: LocalDate, days: Int)

case class DateWithDaysValidator(allowPast: Boolean, minValue: Int = Int.MinValue, maxValue: Int = Int.MaxValue) extends FieldValidator[DateWithDaysValues, DateWithDays] {
  override def validate(vs: DateWithDaysValues): ValidatedNel[String, DateWithDays] = {
    Logger.debug(s"validating $vs")
    import cats.implicits._
    (DateFieldValidator(allowPast).validate(vs.date.getOrElse(DateValues(None, None, None))) |@|
      IntValidator(minValue, maxValue).validate(vs.days.getOrElse(""))).map { case (date, days) =>
      DateWithDays(date, days)
    }
  }
}
