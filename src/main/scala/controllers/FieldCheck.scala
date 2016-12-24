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

package controllers

import forms.validation._
import play.api.Logger
import play.api.libs.json._

trait FieldCheck {
  def apply(path: String, value: JsValue): List[FieldError]

  def hint(path: String, value: JsValue): List[FieldHint]
}

object FieldChecks {
  val mandatoryCheck = new FieldCheck {
    override def apply(path: String, value: JsValue): List[FieldError] =
      MandatoryValidator(None).validate(path, value.validate[String].asOpt).fold(_.toList, _ => List())

    override def hint(path: String, value: JsValue): List[FieldHint] = List()
  }

  val noCheck = new FieldCheck {
    override def hint(path: String, value: JsValue) = List.empty

    override def apply(path: String, value: JsValue) = List.empty
  }

  trait OptionalFieldCheck[T] extends FieldCheck {
    def validator: FieldValidator[Option[String], T]

    override def apply(path: String, value: JsValue): List[FieldError] = validator.validate(path, decodeString(value)).fold(_.toList, _ => List())

    override def hint(path: String, value: JsValue): List[FieldHint] = validator.hintText(path, value.validate[String].asOpt)
  }

  def mandatoryText(wordLimit: Int, displayName: Option[String] = None) = new OptionalFieldCheck[String] {
    override val validator: FieldValidator[Option[String], String] = MandatoryValidator(displayName).andThen(WordCountValidator(wordLimit))
  }

  def intFieldCheck(min: Int, max: Int, displayName: Option[String] = None) = new OptionalFieldCheck[Int] {
    override val validator: FieldValidator[Option[String], Int] = MandatoryValidator(displayName).andThen(IntValidator(min, max))
  }

  def fromValidator[T: Reads](v: FieldValidator[T, _]): FieldCheck = new FieldCheck {
    override def toString: String = s"check from validator $v"

    override def apply(path: String, jv: JsValue) = {
      jv.validate[T].map { x =>
        v.validate(path, x).fold(_.toList, _ => List())
      } match {
        case JsSuccess(msgs, _) => msgs
        case JsError(errs) =>
          Logger.debug(s"could not decode form values from $jv with validator $v on path $path")
          Logger.debug(s"$errs")
          List(FieldError(path, "Could not decode form values!"))
      }
    }

    override def hint(path: String, jv: JsValue): List[FieldHint] = jv.validate[T].asOpt.map(t => v.hintText(path, t)).getOrElse(Nil)
  }


  def decodeString(jv: JsValue): Option[String] = {
    jv match {
      case JsString(s) => Some(s)
      case _ => None
    }
  }
}