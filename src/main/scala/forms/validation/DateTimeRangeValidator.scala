package forms.validation

import cats.data.ValidatedNel
import cats.syntax.cartesian._
import cats.syntax.validated._
import forms.DateValues
import org.joda.time.LocalDate

case class DateTimeRangeValues(startDate: Option[DateValues], endDate: Option[DateValues], hasClosingDate: Option[String])

case class DateTimeRange(startDate: LocalDate, endDate: Option[LocalDate])

case class DateTimeRangeValidator(allowPast: Boolean, isEndDateMandatory: Boolean) extends FieldValidator[DateTimeRangeValues, DateTimeRange] {
  val dateValidator = DateFieldValidator(allowPast)

  override def validate(path: String, vs: DateTimeRangeValues): ValidatedNel[FieldError, DateTimeRange] = {
    val sdv: DateValues = vs.startDate.getOrElse(DateValues(None, None, None))
    val edv: Option[DateValues] = vs.endDate

    val startDateV = dateValidator.validate(s"$path.startDate", sdv)

    // TODO: Check `isEndDateMandatory` as part of the validation. At the moment this just assumes it's optional
    val endDateV: ValidatedNel[FieldError, Option[LocalDate]] =
      edv.map(dateValidator.validate(s"$path.startDate", _)
        .map(Some(_)))
        .getOrElse(None.valid)

    (startDateV |@| endDateV).map(DateTimeRange.apply)
  }
}
