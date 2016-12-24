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

case class MandatoryValidator(displayName: Option[String] = None) extends FieldValidator[Option[String], String] {
  override def normalise(os: Option[String]): Option[String] = os.map(_.trim())

  override def doValidation(path: String, so: Normalised[Option[String]]): ValidatedNel[FieldError, String] = {
    val fieldName = displayName.map(n => s"'$n'").getOrElse("Field")
    denormal(so) match {
      case None | Some("") => FieldError(path, s"$fieldName cannot be empty").invalidNel
      case Some(n) => n.validNel
    }
  }
}
