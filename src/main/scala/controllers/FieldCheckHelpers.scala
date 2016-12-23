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

import forms.validation.{FieldError, FieldHint}
import play.api.Logger
import play.api.libs.json.{JsDefined, JsNull, JsObject, JsValue}


object FieldCheckHelpers {
  type FieldErrors = List[FieldError]
  val noErrors: FieldErrors = List()
  type FieldHints = List[FieldHint]

  def check(fieldValues: JsObject, checks: Map[String, FieldCheck]): FieldErrors = {
    checkList(fieldValues, checks).flatMap { case (n, v, c) => c(n, v) }
  }

  def hinting(fieldValues: JsObject, checks: Map[String, FieldCheck]): FieldHints = {
    checkList(fieldValues, checks).flatMap { case (n, v, c) => c.hint(n, v) }
  }

  def checkList(fieldValues: JsObject, checks: Map[String, FieldCheck]): List[(String, JsValue, FieldCheck)] = {
    checks.toList.map {
      case ("", check) => ("", fieldValues, check)
      case (fieldName, check) =>
        fieldValues \ fieldName match {
          case JsDefined(jv) => (fieldName, jv, check)
          case _ => (fieldName, JsNull, check)
        }
    }
  }
}
