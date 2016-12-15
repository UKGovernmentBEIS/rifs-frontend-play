package forms.validation

import cats.data.ValidatedNel
import cats.syntax.validated._
import forms.validation.FieldValidator.Normalised

import scala.util.Try

object ParseInt {
  def unapply(s: String): Option[Int] = Try(s.toInt).toOption
}

case class IntValidator(minValue: Int = Int.MinValue, maxValue: Int = Int.MaxValue) extends FieldValidator[String, Int] {
  override def normalise(s: String): String = s.trim()

  override def doValidation(path: String, s: Normalised[String]): ValidatedNel[FieldError, Int] = {
    s match {
      case ParseInt(i) if i < minValue => FieldError(path, s"Minimum value is $minValue").invalidNel
      case ParseInt(i) if i > maxValue => FieldError(path, s"Maximum value is $maxValue").invalidNel
      case ParseInt(i) => i.validNel
      case _ => FieldError(path, "Must be a whole number").invalidNel
    }
  }
}
