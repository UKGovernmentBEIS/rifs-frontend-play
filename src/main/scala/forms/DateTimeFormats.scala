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
