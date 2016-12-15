package forms.validation

import cats.data.ValidatedNel
import cats.syntax.validated._
import forms.validation.FieldValidator.Normalised

case class MandatoryValidator(displayName: Option[String] = None) extends FieldValidator[Option[String], String] {
  override def normalise(os: Option[String]): Option[String] = os.map(_.trim())

  override def doValidation(path: String, so: Normalised[Option[String]]): ValidatedNel[FieldError, String] = {
    val fieldName = displayName.map(n => s"'$n'").getOrElse("Field")
    denormal(so) match {
      case None | Some("") => FieldError(path, s"$fieldName cannot be empty").invalidNel
      case Some(n) => n.validNel
    }
  }
}
