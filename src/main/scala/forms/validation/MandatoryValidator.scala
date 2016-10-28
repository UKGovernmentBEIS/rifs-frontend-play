package forms.validation

import cats.data.ValidatedNel
import cats.syntax.validated._

case object MandatoryValidator extends FieldValidator[Option[String], String] {
  override def normalise(os: Option[String]): Option[String] = os.map(_.trim())

  override def validate(path: String, s: Option[String]): ValidatedNel[FieldError, String] = {
    val fieldName = path.split('.').reverse.headOption.map(n => s"'$n'").getOrElse("Field")
    normalise(s) match {
      case None | Some("") => FieldError(path, s"$fieldName cannot be empty").invalidNel
      case Some(n) => n.validNel
    }
  }
}
