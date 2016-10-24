package forms


import cats.data.ValidatedNel
import cats.implicits._
import org.joda.time.LocalDate
import play.api.Logger
import play.api.libs.json.{JsObject, JsString, JsValue}

import scala.util.Try

object ParseInt {
  def unapply(s: String): Option[Int] = Try(s.toInt).toOption
}

case object DateRule extends FieldRule {
  protected def stringValue(o: JsObject, n: String): Option[String] = (o \ n).validate[JsString].asOpt.map(_.value)

  override def validate(value: JsValue): Seq[String] = {
    Logger.debug(s"validating $value")
    val o = value.validate[JsObject].asOpt.getOrElse(JsObject(Seq()))

    val result: ValidatedNel[String, Option[String]] = (validateText(o, "day") |@| validateText(o, "month") |@| validateText(o, "year")).map { case (d, m, y) =>
      Try(new LocalDate(y, m, d)).toOption match {
        case Some(ld) => None
        case None => Some("Must provide a valid date")
      }
    }

    result.fold(errs => errs.toList, eo => eo.toList)
  }

  def validateText(o: JsObject, name: String): ValidatedNel[String, Int] = {

    stringValue(o, name).getOrElse("") match {
      case s if s.trim() == "" => s"'$name' cannot be empty".invalidNel
      case ParseInt(i) => i.validNel
      case _ => s"'$name' must be a number".invalidNel
    }
  }


  override def helpText(value: JsValue): Option[String] = None
}
