package forms.validation

import cats.data.ValidatedNel
import cats.syntax.validated._

import scala.util.Try

object CurrencyValidator extends FieldValidator[Option[String], BigDecimal] {
  override def normalise(os: Option[String]): Option[String] = os.map(_.trim().replaceAll(",", ""))

  override def validate(path: String, value: Option[String]): ValidatedNel[FieldError, BigDecimal] = {
    Try(BigDecimal(normalise(value).getOrElse("")).setScale(2, BigDecimal.RoundingMode.HALF_UP)).toOption match {
      case Some(bd) => bd.validNel
      case None => FieldError(path, "Must be a valid currency value").invalidNel
    }
  }
}
