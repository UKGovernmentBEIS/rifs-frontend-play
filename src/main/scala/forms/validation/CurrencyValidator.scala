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
import cats.syntax.validated._
import forms.validation.FieldValidator.Normalised

import scala.util.Try

object CurrencyValidator {
  def apply() = new CurrencyValidator(None)

  def apply(minValue: BigDecimal) = new CurrencyValidator(Some(minValue))

  final val greaterThanZero = apply(BigDecimal(0.0))
  final val anyValue = apply()
}

class CurrencyValidator(minValue: Option[BigDecimal]) extends FieldValidator[Option[String], BigDecimal] {
  override def normalise(os: Option[String]): Option[String] = os.map(_.trim().replaceAll(",", ""))

  override def doValidation(path: String, value: Normalised[Option[String]]): ValidatedNel[FieldError, BigDecimal] = {
    Try(BigDecimal(value.getOrElse("")).setScale(2, BigDecimal.RoundingMode.HALF_UP)).toOption match {
      case Some(bd) => minValue match {
        case Some(min) if bd <= min => FieldError(path, s"The value must be greater than $min").invalidNel
        case _ => bd.validNel
      }
      case None => FieldError(path, "Must be a valid currency value").invalidNel
    }
  }
}
