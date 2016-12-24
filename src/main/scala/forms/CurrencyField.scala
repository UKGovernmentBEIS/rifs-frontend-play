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

package forms

import controllers.{FieldCheck, FieldChecks, JsonHelpers}
import forms.validation.{CurrencyValidator, FieldError, FieldHint}
import models._
import play.api.libs.json._

case class CurrencyField(label: Option[String], name: String, validator: CurrencyValidator = CurrencyValidator.anyValue) extends Field {
  implicit val osReads = new Reads[Option[String]] {
    override def reads(json: JsValue): JsResult[Option[String]] =
      json match {
        case JsNull => JsSuccess(None)
        case js => js.validate[JsString].map(js => Option(js.value))
      }
  }

  override val check: FieldCheck = FieldChecks.fromValidator(validator)

  override def renderFormInput(questions: Map[String, Question], answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]) =
    views.html.renderers.currencyField(this, questions, JsonHelpers.flatten(answers), errs, hints)

  override def renderPreview(questions: Map[String, Question], answers: JsObject) =
    views.html.renderers.preview.currencyField(this, JsonHelpers.flatten(answers))
}
