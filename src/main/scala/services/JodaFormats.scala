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

package services

import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

trait JodaFormats {

  implicit val jodaLocalDateTimeFormat = new Format[LocalDateTime] {
    val dtf = DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss")

    override def writes(o: LocalDateTime): JsValue = JsString(dtf.print(o))

    override def reads(json: JsValue): JsResult[LocalDateTime] =
      implicitly[Reads[JsString]].reads(json).flatMap { js =>
        Try(dtf.parseLocalDateTime(js.value)) match {
          case Success(s) => JsSuccess(s)
          case Failure(t) => JsError(t.getMessage)
        }
      }
  }
}
