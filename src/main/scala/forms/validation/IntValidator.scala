package forms.validation

import cats.data.ValidatedNel
import cats.syntax.validated._

import scala.util.Try

object ParseInt {
  def unapply(s: String): Option[Int] = Try(s.toInt).toOption
}

case class IntValidator(minValue: Int = Int.MinValue, maxValue: Int = Int.MaxValue) extends FieldValidator[String, Int] {
  override def normalise(s: String): String = s.trim()

  override def validate(s: String): ValidatedNel[String, Int] = {
    normalise(s) match {
      case ParseInt(i) if i < minValue => s"Minimum value is $minValue".invalidNel
      case ParseInt(i) if i > maxValue => s"Maximum value is $maxValue".invalidNel
      case ParseInt(i) => i.validNel
      case _ => "Must be a whole number".invalidNel
    }
  }
}
