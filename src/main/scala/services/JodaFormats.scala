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
