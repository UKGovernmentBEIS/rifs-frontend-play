/*
 * Copyright (C) 2016  Department for Business, Energy and Industrial Strategy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package forms.validation

import cats.data.ValidatedNel
import cats.syntax.cartesian._
import cats.syntax.validated._
import forms.DateValues
import forms.validation.FieldValidator.Normalised
import org.joda.time.LocalDate

/**
  *
  * This is a pretty complex set of validations as we need to not only validate the dates themselves, but
  * also check two different flags that indicate whether an end date should be present. One is the flag
  * on the field itself to day whether the end date is mandatory. The other is a flag that comes from the
  * form input. In the case where the end date is optional then the user can select a radio button to
  * say if they are supplying it.
  *
  * The validations are built up in two layers. The first layer is checking the contents of the
  * `DateTimeRangeValues` structure, which represents the values that have come from the form. So,
  * do the supplied values for the date fields represent valid dates and if the flag to say the end date is
  * supplied is true, then is the end date actually present. If these validations pass then we get
  * a `DateTimeRange` structure out of it where the date fields have been converted to `LocalDate`s
  *
  * Then we apply validations to check that the end date is present if it is marked as mandatory, and that
  * the start date is before the end date.
  *
  * Despite the name (`DateTimeRangeValidator`) it doesn't currently handle time fields because we're not
  * sure how that's going to look yet.
  *
  * @param endDateProvided corresponds to the checkbox on the field renderer where the user selects whether
  *                        they are entering a closing date. This lets us distinguish between the user saying
  *                        that they are not providing a date, vs. saying they are but leaving it blank.
  */
case class DateTimeRangeValues(startDate: Option[DateValues], endDate: Option[DateValues], endDateProvided: Option[String])

case class DateTimeRange(startDate: LocalDate, endDate: Option[LocalDate])

case class DateTimeRangeValidator(allowPast: Boolean, isEndDateMandatory: Boolean) extends FieldValidator[DateTimeRangeValues, DateTimeRange] {
  val dateValidator = DateFieldValidator(allowPast)

  val mustProvideValidStartDateMessage = "You must provide a valid start date"
  val mustProvideValidEndDateMessage = "You must provide a valid end date"
  val endMustBeLaterThanStartMessage = "End date must be later than the start date"

  /**
    * Check that the individual form values are valid and build up a `DateTimeRange` instance that
    * can be further validated.
    */
  lazy val fieldLevelValidations = new FieldValidator[DateTimeRangeValues, DateTimeRange] {
    override def doValidation(path: String, vs: Normalised[DateTimeRangeValues]): ValidatedNel[FieldError, DateTimeRange] = {
      val sdv: DateValues = vs.startDate.getOrElse(DateValues(None, None, None))

      // If no text values are provided for day/month/year of end date then treat it as
      // if no end date was provided at all.
      val edvo: Option[DateValues] = vs.endDate match {
        case Some(DateValues(Some(""), Some(""), Some(""))) | Some(DateValues(None, None, None)) => None
        case v => v
      }

      val startDateV: ValidatedNel[FieldError, LocalDate] = dateValidator.validate(s"$path.startDate", sdv)

      // First check that the end date is valid if it's present
      val endDateValid = edvo.map(dateValidator.validate(s"$path.endDate", _).map(Some(_))).getOrElse(None.valid)

      // And then check if it's present if the `endDateProvided` flag is set
      val endDateV = endDateValid.map(od => (od, vs.endDateProvided.exists(_.trim == "yes"))).andThen(endDateIsPresentIfSupplied.validate(path, _))

      (startDateV |@| endDateV).map(DateTimeRange.apply)
    }
  }

  /**
    * Check that if the `isEndDateMandatory` flag is `true` that an end date is present on the form. This is
    * irrespective of the value of `endDateProvided`.
    */
  lazy val endDateIsPresentIfMandatory = new FieldValidator[DateTimeRange, DateTimeRange] {
    override def doValidation(path: String, vs: Normalised[DateTimeRange]): ValidatedNel[FieldError, DateTimeRange] = {
      (isEndDateMandatory, vs.endDate) match {
        case (true, None) => FieldError(s"$path.endDate", mustProvideValidEndDateMessage).invalidNel
        case _ => vs.valid
      }
    }
  }

  /**
    * Check that, if the form values indicate that the user has said they're providing an end date,
    * then the user has actually provided values for the end date.
    */
  lazy val endDateIsPresentIfSupplied = new FieldValidator[(Option[LocalDate], Boolean), Option[LocalDate]] {
    override def doValidation(path: String, vs: Normalised[(Option[LocalDate], Boolean)]) = {
      denormal(vs) match {
        case (None, true) => FieldError(s"$path.endDate", mustProvideValidEndDateMessage).invalidNel
        case _ => vs._1.valid
      }
    }
  }

  lazy val startDateIsBeforeEndDate = new FieldValidator[DateTimeRange, DateTimeRange] {
    override def doValidation(path: String, dtr: Normalised[DateTimeRange]): ValidatedNel[FieldError, DateTimeRange] = {
      dtr.endDate.map(_.isAfter(dtr.startDate)) match {
        case Some(false) =>
          FieldError(path, endMustBeLaterThanStartMessage).invalidNel
        case _ => dtr.valid
      }
    }
  }

  override def doValidation(path: String, vs: Normalised[DateTimeRangeValues]): ValidatedNel[FieldError, DateTimeRange] = {
    (fieldLevelValidations andThen endDateIsPresentIfMandatory andThen startDateIsBeforeEndDate).validate(path, vs)
  }
}
