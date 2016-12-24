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
import forms.validation._
import models.Question
import play.api.libs.json.{JsObject, Json}

case class DateTimeRangeField(name: String, allowPast: Boolean, isEndDateMandatory: Boolean) extends Field with DateTimeFormats {
  implicit val dvReads = Json.reads[DateValues]
  implicit val dtrReads = Json.reads[DateTimeRangeValues]

  val startDateField = DateField(s"$name.startDate", allowPast)
  val endDateField = DateField(s"$name.endDate", allowPast)

  val validator = DateTimeRangeValidator(allowPast = allowPast, isEndDateMandatory = isEndDateMandatory)

  override val check: FieldCheck = FieldChecks.fromValidator(validator)

  override def renderFormInput(questions: Map[String, Question], answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]) =
    views.html.renderers.dateTimeRangeField(this, questions, answers, errs, hints)

  override def renderPreview(questions: Map[String, Question], answers: JsObject) = {
    val flattenedAnswers = JsonHelpers.flatten("", answers)
    val startDateValues = dateValuesFor(s"${startDateField.name}", flattenedAnswers)
    val endDateValues = dateValuesFor(s"${endDateField.name}", flattenedAnswers)
    val vs = DateTimeRangeValues(Some(startDateValues), Some(endDateValues), endDateProvided = None)

    validator.validate("", vs).map { dtr =>
      views.html.renderers.preview.dateTimeRangeField(
        this,
        fmt.print(dtr.startDate),
        accessFmt.print(dtr.startDate),
        dtr.endDate.map(fmt.print),
        dtr.endDate.map(accessFmt.print))
    }.leftMap { errs =>
      views.html.renderers.preview.dateTimeRangeField(this, "None", "None", None, None)
    }.fold(identity, identity)
  }

  def dateValuesFor(name: String, answers: Map[String, String]): DateValues =
    DateValues(answers.get(s"$name.day"), answers.get(s"$name.month"), answers.get(s"$name.year"))

}
