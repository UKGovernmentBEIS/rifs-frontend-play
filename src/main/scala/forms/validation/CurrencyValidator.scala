package forms.validation

import cats.data.ValidatedNel
import cats.syntax.validated._

import scala.util.Try

class CurrencyValidator(maximumValue: BigDecimal) extends FieldValidator[String, BigDecimal] {
  override def normalise(s: String): String = s.trim().replaceAll("[,].", "")

  override def validate(path: String, value: String): ValidatedNel[FieldError, BigDecimal] = {
    Try(BigDecimal(value)).toOption match {
      case Some(bd) => bd.setScale(2, BigDecimal.RoundingMode.HALF_UP).validNel
      case None => FieldError(path, "Must be a valid currency value").invalidNel
    }
  }
}
