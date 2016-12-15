package forms.validation

import cats.data.ValidatedNel
import cats.syntax.cartesian._
import forms.DateValues
import forms.validation.FieldValidator.Normalised
import org.joda.time.LocalDate

case class DateWithDaysValues(date: Option[DateValues], days: Option[String])

case class DateWithDays(date: LocalDate, days: Int)

case class DateWithDaysValidator(allowPast: Boolean, minValue: Int, maxValue: Int) extends FieldValidator[DateWithDaysValues, DateWithDays] {

  val dateValidator = DateFieldValidator(allowPast)
  val daysValidator = IntValidator(minValue, maxValue)

  override def doValidation(path: String, vs: Normalised[DateWithDaysValues]): ValidatedNel[FieldError, DateWithDays] = {
    val dv: DateValues = vs.date.getOrElse(DateValues(None, None, None))
    val days: String = vs.days.getOrElse("")

    val dateV: ValidatedNel[FieldError, LocalDate] = dateValidator.validate(s"$path.date", dv)
    val daysV: ValidatedNel[FieldError, Int] = daysValidator.validate(s"$path.days", days)

    (dateV |@| daysV).map(DateWithDays.apply)
  }
}
