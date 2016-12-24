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

import org.joda.time.LocalDate
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

trait DateTimeFormats {
  val fmt: DateTimeFormatter = DateTimeFormat.forPattern("d MMMM yyyy")
  val accessFmt: AccessibleDateTimeFormat = AccessibleDateTimeFormat()
}

case class AccessibleDateTimeFormat() {
  val inner: DateTimeFormatter = DateTimeFormat.forPattern("MMMM yyyy")

  def print(date: LocalDate): String = {
    val x = date.getDayOfMonth match {
      case n if Seq(11, 12, 13) contains n => n + "th"
      case n if n % 10 == 1 => n + "st"
      case n if n % 10 == 2 => n + "nd"
      case n if n % 10 == 3 => n + "rd"
      case n => n + "th"
    }
    s"$x of ${inner.print(date)}"
  }
}
