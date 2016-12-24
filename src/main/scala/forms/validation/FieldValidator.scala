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
import shapeless.tag._

case class FieldError(path: String, err: String)

case class FieldHint(path: String, hint: String, matchingJsType: Option[String] = None, matchingJsConfig: Option[String] = None)

object FieldValidator {

  sealed trait NormalisedTag

  type Normalised[T] = T @@ NormalisedTag
}

trait FieldValidator[A, B] {
  outer =>

  import FieldValidator._


  /**
    * Provide a way for the validator to normalise the input value. To keep things simple for the validator
    * implementation this returns an `A` rather than a `Normalised[A]` and the `validate` method does the
    * tagging.
    */
  def normalise(a: A): A = a

  /**
    * Sometimes the type tag confuses the compiler, usually in `match` expressions. This
    * convenience function strips the tag from the normalised value for those situations.
    */
  final def denormal(a: Normalised[A]): A = a

  private def normaliseAndTag(a: A): Normalised[A] = normalise(a).asInstanceOf[A @@ NormalisedTag]

  final def validate(path: String, a: A): ValidatedNel[FieldError, B] = doValidation(path, normaliseAndTag(a))

  protected def doValidation(path: String, a: Normalised[A]): ValidatedNel[FieldError, B]

  final def hintText(path: String, a: A): List[FieldHint] = doHinting(path, normaliseAndTag(a))

  protected def doHinting(path: String, a: Normalised[A]): List[FieldHint] = List()

  def andThen[C](v2: FieldValidator[B, C]): FieldValidator[A, C] = new FieldValidator[A, C] {
    override def doValidation(path: String, a: Normalised[A]): ValidatedNel[FieldError, C] = outer.validate(path, a).andThen(v2.validate(path, _))

    override def doHinting(path: String, a: Normalised[A]): List[FieldHint] = {
      val bHints = outer.validate(path, a).map(b => v2.hintText(path, b)).valueOr(_ => Nil)
      bHints ++ outer.hintText(path, a)
    }
  }
}





