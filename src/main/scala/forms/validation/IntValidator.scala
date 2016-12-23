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

object ParseInt {
  def unapply(s: String): Option[Int] = Try(s.toInt).toOption
}

case class IntValidator(minValue: Int = Int.MinValue, maxValue: Int = Int.MaxValue) extends FieldValidator[String, Int] {
  override def normalise(s: String): String = s.trim()

  override def doValidation(path: String, s: Normalised[String]): ValidatedNel[FieldError, Int] = {
    s match {
      case ParseInt(i) if i < minValue => FieldError(path, s"Minimum value is $minValue").invalidNel
      case ParseInt(i) if i > maxValue => FieldError(path, s"Maximum value is $maxValue").invalidNel
      case ParseInt(i) => i.validNel
      case _ => FieldError(path, "Must be a whole number").invalidNel
    }
  }
}
