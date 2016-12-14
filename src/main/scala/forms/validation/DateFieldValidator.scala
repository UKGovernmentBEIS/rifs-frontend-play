package forms.validation

import cats.data.{NonEmptyList, ValidatedNel}
import cats.syntax.cartesian._
import cats.syntax.validated._
import forms.DateValues
import org.joda.time.LocalDate

import scala.util.Try

case class DMY(day: Int, month: Int, year: Int)

object DateFieldValidator {
  val mustProvideAValidDateMsg = "Must provide a valid date"
  val mustBeTodayOrLaterMsg = "Must be today or later"
}

case class DateFieldValidator(allowPast: Boolean) extends FieldValidator[DateValues, LocalDate] {

  import DateFieldValidator._

  override def normalise(vs: DateValues): DateValues = vs.copy(
    day = vs.day.map(_.trim()),
    month = vs.month.map(_.trim()),
    year = vs.year.map(_.trim())
  )

  def mandatoryInt(path: String, s: Option[String], displayName: String): ValidatedNel[FieldError, Int] = MandatoryValidator(Some(displayName)).validate(path, s).andThen(IntValidator().validate(path, _))

  def validateDMY(path: String, vs: DateValues): ValidatedNel[FieldError, DMY] = {
    (mandatoryInt(s"$path.day", vs.day, "day") |@|
      mandatoryInt(s"$path.month", vs.month, "month") |@|
      mandatoryInt(s"$path.year", vs.year, "year")).tupled
      .map { case (d, m, y) => DMY(d, m, y) }
      .leftMap(_ => NonEmptyList.of(FieldError(s"$path", mustProvideAValidDateMsg)))
  }

  def validateDate(path: String, dmy: DMY): ValidatedNel[FieldError, LocalDate] =
    Try(new LocalDate(dmy.year, dmy.month, dmy.day)).toOption match {
      case Some(ld) => ld.valid
      case None => FieldError(path, mustProvideAValidDateMsg).invalidNel
    }

  def validatePastDate(path: String, ld: LocalDate): ValidatedNel[FieldError, LocalDate] =
    if (!allowPast && ld.isBefore(LocalDate.now())) FieldError(path, mustBeTodayOrLaterMsg).invalidNel
    else ld.valid

  override def validate(path: String, vs: DateValues): ValidatedNel[FieldError, LocalDate] =
    validateDMY(path, normalise(vs)).andThen(validateDate(path, _)).andThen(validatePastDate(path, _))
}
