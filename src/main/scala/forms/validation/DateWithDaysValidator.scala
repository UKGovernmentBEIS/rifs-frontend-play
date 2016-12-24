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
