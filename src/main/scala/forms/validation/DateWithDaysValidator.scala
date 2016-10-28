package forms.validation

import cats.data.ValidatedNel
import forms.DateValues
import org.joda.time.LocalDate

case class DateWithDaysValues(date: Option[DateValues], days: Option[String])

case class DateWithDays(date: LocalDate, days: Int)

case class DateWithDaysValidator(allowPast: Boolean, minValue: Int = Int.MinValue, maxValue: Int = Int.MaxValue) extends FieldValidator[DateWithDaysValues, DateWithDays] {

  val dateValidator = DateFieldValidator(allowPast)
  val daysValidator = IntValidator(minValue, maxValue)

  override def validate(path: String, vs: DateWithDaysValues): ValidatedNel[FieldError, DateWithDays] = {
    val dv: DateValues = vs.date.getOrElse(DateValues(None, None, None))
    val days: String = vs.days.getOrElse("")

    val dateV: ValidatedNel[FieldError, LocalDate] = dateValidator.validate(s"$path.date", dv)
    val daysV: ValidatedNel[FieldError, Int] = daysValidator.validate(s"$path.days", days)

    // IDEA doesn't think this import is used - take care not to optimise it away! I've duplicated
    // it in a comment so it's easy to restore if you lose it by mistake.
    //import cats.syntax.cartesian._
    import cats.syntax.cartesian._
    (dateV |@| daysV).map(DateWithDays.apply _)
  }
}
