package forms.validation

import cats.data.ValidatedNel
import cats.implicits._
import forms.DateValues
import org.joda.time.LocalDate

import scala.util.Try

class DateFieldValidator(allowPast: Boolean) extends FieldValidator[DateValues, LocalDate] {

  override def normalise(vs: DateValues): DateValues = vs.copy(
    day = vs.day.map(_.trim()),
    month = vs.month.map(_.trim()),
    year = vs.year.map(_.trim())
  )

  def mandatoryInt(s: Option[String]): ValidatedNel[String, Int] = MandatoryValidator.validate(s).andThen(IntValidator().validate(_))

  def validateDate(d: Int, m: Int, y: Int): ValidatedNel[String, LocalDate] =
    Try(new LocalDate(y, m, d)).toOption match {
      case Some(ld) if !allowPast && ld.isBefore(LocalDate.now()) => "Must be today or later".invalidNel
      case Some(ld) => ld.valid
      case None => "Must provide a valid date".invalidNel
    }

  override def validate(vs: DateValues): ValidatedNel[String, LocalDate] =
    (mandatoryInt(vs.day) |@| mandatoryInt(vs.month) |@| mandatoryInt(vs.year))
      .map { case (d, m, y) => (d, m, y) }
      .andThen { case (d, m, y) => validateDate(d, m, y) }

}
