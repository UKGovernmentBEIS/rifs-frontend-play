package forms.validation

import cats.data.ValidatedNel
import cats.syntax.validated._

case object MandatoryValidator extends FieldValidator[Option[String], String] {
  override def normalise(os: Option[String]): Option[String] = os.map(_.trim())

  override def validate(s: Option[String]): ValidatedNel[String, String] = {
    normalise(s) match {
      case None | Some("") => "Field cannot be empty".invalidNel
      case Some(n) => n.validNel
    }
  }
}
