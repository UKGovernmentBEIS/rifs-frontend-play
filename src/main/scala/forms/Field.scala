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

import controllers.FieldCheck
import forms.validation.{FieldError, FieldHint}
import models._
import play.api.libs.json.JsObject
import play.twirl.api.Html

trait Field {
  def renderFormInput(questions: Map[String, Question], answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html

  def renderPreview(questions: Map[String, Question], answers: JsObject): Html

  def name: String

  def check: FieldCheck

  /**
    * Allow for more relaxed checks on preview if needed
    */
  def previewCheck: FieldCheck = check

}

