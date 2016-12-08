package forms.validation

import cats.data.ValidatedNel
import cats.syntax.validated._

import scala.util.Try

object CurrencyValidator {
  def apply() = new CurrencyValidator(None)
  def apply(minValue: BigDecimal) = new CurrencyValidator(Some(minValue))
  final val positiveOnly = apply( BigDecimal(0.0) )
}

class CurrencyValidator(minValue: Option[BigDecimal]) extends FieldValidator[Option[String], BigDecimal] {
  override def normalise(os: Option[String]): Option[String] = os.map(_.trim().replaceAll(",", ""))

  override def validate(path: String, value: Option[String]): ValidatedNel[FieldError, BigDecimal] = {
    Try(BigDecimal(normalise(value).getOrElse("")).setScale(2, BigDecimal.RoundingMode.HALF_UP)).toOption match {
      case Some(bd) => minValue match {
        case Some(min) if bd <= min => FieldError(path, s"The value must be greater than $min").invalidNel
        case _ => bd.validNel
      }
      case None => FieldError(path, "Must be a valid currency value").invalidNel
    }
  }
}
