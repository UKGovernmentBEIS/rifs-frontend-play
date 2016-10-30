package forms.validation

import cats.data.ValidatedNel
import cats.syntax.validated._

case class MandatoryValidator(displayName: Option[String] = None) extends FieldValidator[Option[String], String] {
  override def normalise(os: Option[String]): Option[String] = os.map(_.trim())

  override def validate(path: String, s: Option[String]): ValidatedNel[FieldError, String] = {
    val fieldName = displayName.map(n => "'$n'").getOrElse("Field")
    normalise(s) match {
      case None | Some("") => FieldError(path, s"$fieldName cannot be empty").invalidNel
      case Some(n) => n.validNel
    }
  }
}
