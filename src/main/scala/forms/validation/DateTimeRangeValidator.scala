package forms.validation

import cats.data.ValidatedNel
import cats.syntax.cartesian._
import cats.syntax.validated._
import forms.DateValues
import org.joda.time.LocalDate

/**
  *
  * @param endDateProvided corresponds to the checkbox on the field renderer where the user selects whether
  *                        they are entering a closing date. This lets us distinguish between the user saying
  *                        that they are not providing a date, vs. saying they are but leaving it blank.
  */
case class DateTimeRangeValues(startDate: Option[DateValues], endDate: Option[DateValues], endDateProvided: Boolean)

case class DateTimeRange(startDate: LocalDate, endDate: Option[LocalDate])

case class DateTimeRangeValidator(allowPast: Boolean, isEndDateMandatory: Boolean) extends FieldValidator[DateTimeRangeValues, DateTimeRange] {
  val dateValidator = DateFieldValidator(allowPast)

  val mandatoryEndDateIsPresentV = new FieldValidator[DateTimeRangeValues, DateTimeRangeValues] {
    override def validate(path: String, vs: DateTimeRangeValues): ValidatedNel[FieldError, DateTimeRangeValues] = {
      (isEndDateMandatory, vs.endDate) match {
        case (true, None) => FieldError(path, "End date must be supplied").invalidNel
        case _ => vs.valid
      }
    }
  }

  val fieldLevelV = new FieldValidator[DateTimeRangeValues, DateTimeRange] {
    override def validate(path: String, vs: DateTimeRangeValues): ValidatedNel[FieldError, DateTimeRange] = {
      val sdv: DateValues = vs.startDate.getOrElse(DateValues(None, None, None))
      val edv: Option[DateValues] = vs.endDate

      val startDateV = dateValidator.validate(s"$path.startDate", sdv)
      val endDateV = edv.map(dateValidator.validate(s"$path.endDate", _).map(Some(_))).getOrElse(None.valid)
      val mandatoryV = mandatoryEndDateIsPresentV.validate(path, vs)

      (mandatoryV |@| startDateV |@| endDateV).map((_, sd, ev) => DateTimeRange.apply(sd, ev))
    }
  }

  val startDateBeforeEndDateV = new FieldValidator[DateTimeRange, DateTimeRange] {
    override def validate(path: String, dtr: DateTimeRange): ValidatedNel[FieldError, DateTimeRange] = {
      dtr.endDate.map(_.isAfter(dtr.startDate)) match {
        case Some(false) => FieldError(path, "End date must be later than the start date").invalidNel
        case _ => dtr.valid
      }
    }
  }

  override def validate(path: String, vs: DateTimeRangeValues): ValidatedNel[FieldError, DateTimeRange] = {
    fieldLevelV.andThen(startDateBeforeEndDateV).validate(path, vs)
  }
}
