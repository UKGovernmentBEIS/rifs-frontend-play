package forms.validation

import cats.data.ValidatedNel
import cats.syntax.validated._

import scala.util.Try

object CurrencyValidator extends FieldValidator[String, BigDecimal] {
  override def normalise(s: String): String = s.trim().replaceAll(",", "")

  override def validate(path: String, value: String): ValidatedNel[FieldError, BigDecimal] = {
    Try(BigDecimal(normalise(value)).setScale(2, BigDecimal.RoundingMode.HALF_UP)).toOption match {
      case Some(bd) => bd.validNel
      case None => FieldError(path, "Must be a valid currency value").invalidNel
    }
  }
}
