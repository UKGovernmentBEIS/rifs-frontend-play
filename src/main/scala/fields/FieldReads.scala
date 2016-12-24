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

package fields

import forms._
import play.api.libs.json._

case class FieldType(`type`: String)

object FieldReads {

  implicit val fieldTypeReads = Json.reads[FieldType]
  implicit val textFieldReads = Json.reads[TextField]
  implicit val textAreaFieldReads = Json.reads[TextAreaField]
  implicit val dateWithDaysReads = Json.reads[DateWithDaysField]
  implicit val dateTimeRangeReads = Json.reads[DateTimeRangeField]
  implicit val costItemReads = Json.reads[CostItemField]

  implicit object fieldReads extends Reads[Field] {
    override def reads(json: JsValue): JsResult[Field] = {
      json.validate[FieldType].flatMap { o =>
        o.`type` match {
          case "text" => json.validate[TextField]
          case "textArea" => json.validate[TextAreaField]
          case "dateWithDays" => json.validate[DateWithDaysField]
          case "dateTimeRange" => json.validate[DateTimeRangeField]
          case "costItem" => json.validate[CostItemField]
          case t => JsError(s"unknown field type $t")
        }
      }
    }
  }

}
