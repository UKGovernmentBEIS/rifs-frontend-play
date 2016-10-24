package forms

import play.api.libs.json.{JsObject, JsString, JsValue}

import scala.util.Try

object ParseInt {
  def unapply(s: String): Option[Int] = Try(s.toInt).toOption
}

class DateRule extends FieldRule {
  protected def stringValue(o: JsObject, n: String): Option[String] = (o \ n).validate[JsString].asOpt.map(_.value)

  override def validate(value: JsValue): Seq[String] = {
    value.validate[JsObject].asOpt.flatMap { o =>
      stringValue(o, "day") flatMap {
        case ParseInt(i) => None
        case _ => Some("'day' must be a number")
      }
    }
  }.toSeq

  override def helpText(value: JsValue): Option[String] = None
}
