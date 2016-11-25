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
case class DateTimeRangeValues(startDate: Option[DateValues], endDate: Option[DateValues], endDateProvided: Option[Boolean])

case class DateTimeRange(startDate: LocalDate, endDate: Option[LocalDate])

case class DateTimeRangeValidator(allowPast: Boolean, isEndDateMandatory: Boolean) extends FieldValidator[DateTimeRangeValues, DateTimeRange] {
  val dateValidator = DateFieldValidator(allowPast)

  val mustProvideValidEndDateMessage = "You must provide a valid end date"
  val endMustBeLaterThanStartMessage = "End date must be later than the start date"

  /**
    * Check that the individual form values are valid and build up a `DateTimeRange` instance that
    * can be further validated.
    */
  val fieldLevelValidations = new FieldValidator[DateTimeRangeValues, DateTimeRange] {
    override def validate(path: String, vs: DateTimeRangeValues): ValidatedNel[FieldError, DateTimeRange] = {
      val sdv: DateValues = vs.startDate.getOrElse(DateValues(None, None, None))
      val edv: Option[DateValues] = vs.endDate

      val startDateV = dateValidator.validate(s"$path.startDate", sdv)
      // First check that the end date is valid if it's present
      val endDateValid = edv.map(dateValidator.validate(s"$path.endDate", _).map(Some(_))).getOrElse(None.valid)
      // And then check if it's present if the `endDateProvided` flag is set
      val endDateV = endDateValid.map(od => (od, vs.endDateProvided.getOrElse(false))).andThen(endDateIsPresentIfSupplied.validate(path, _))

      (startDateV |@| endDateV).map(DateTimeRange.apply)
    }
  }

  /**
    * Check that if the `isEndDateMandatory` flag is `true` that an end date is present on the form. This is
    * irrespective of the value of `endDateProvided`.
    */
  val endDateIsPresentIfMandatory = new FieldValidator[DateTimeRange, DateTimeRange] {
    override def validate(path: String, vs: DateTimeRange): ValidatedNel[FieldError, DateTimeRange] = {
      (isEndDateMandatory, vs.endDate) match {
        case (true, None) =>

          FieldError(path, mustProvideValidEndDateMessage).invalidNel
        case _ => vs.valid
      }
    }
  }

  /**
    * Check that, if the form values indicate that the user has said they're providing an end date,
    * then the user has actually provided values for the end date.
    */
  val endDateIsPresentIfSupplied = new FieldValidator[(Option[LocalDate], Boolean), Option[LocalDate]] {
    override def validate(path: String, vs: (Option[LocalDate], Boolean)) = {
      vs match {
        case (None, true) => FieldError(path, mustProvideValidEndDateMessage).invalidNel
        case _ => vs._1.valid
      }
    }
  }

  val startDateIsBeforeEndDate = new FieldValidator[DateTimeRange, DateTimeRange] {
    override def validate(path: String, dtr: DateTimeRange): ValidatedNel[FieldError, DateTimeRange] = {
      dtr.endDate.map(_.isAfter(dtr.startDate)) match {
        case Some(false) =>
          FieldError(path, endMustBeLaterThanStartMessage).invalidNel
        case _ => dtr.valid
      }
    }
  }

  override def validate(path: String, vs: DateTimeRangeValues): ValidatedNel[FieldError, DateTimeRange] = {
    (fieldLevelValidations andThen endDateIsPresentIfMandatory andThen startDateIsBeforeEndDate).validate(path, vs)
  }
}
