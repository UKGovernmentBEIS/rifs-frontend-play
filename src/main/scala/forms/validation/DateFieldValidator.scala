package forms.validation

import cats.data.ValidatedNel
import cats.implicits._
import forms.DateValues
import org.joda.time.LocalDate

import scala.util.Try

case class DateFieldValidator(allowPast: Boolean) extends FieldValidator[DateValues, LocalDate] {

  override def normalise(vs: DateValues): DateValues = vs.copy(
    day = vs.day.map(_.trim()),
    month = vs.month.map(_.trim()),
    year = vs.year.map(_.trim())
  )

  def mandatoryInt(path: String, s: Option[String], displayName: String): ValidatedNel[FieldError, Int] = MandatoryValidator(Some(displayName)).validate(path, s).andThen(IntValidator().validate(path, _))

  def validateDate(path: String, d: Int, m: Int, y: Int): ValidatedNel[FieldError, LocalDate] =
    Try(new LocalDate(y, m, d)).toOption match {
      case Some(ld) if !allowPast && ld.isBefore(LocalDate.now()) => FieldError(path, "Must be today or later").invalidNel
      case Some(ld) => ld.valid
      case None => FieldError(path, "Must provide a valid date").invalidNel
    }

  override def validate(path: String, vs: DateValues): ValidatedNel[FieldError, LocalDate] =
    (mandatoryInt(s"$path.day", vs.day, "day") |@| mandatoryInt(s"$path.month", vs.month, "month") |@| mandatoryInt(s"$path.year", vs.year, "year"))
      .map { case (d, m, y) => (d, m, y) }
      .andThen { case (d, m, y) => validateDate(path, d, m, y) }

}
