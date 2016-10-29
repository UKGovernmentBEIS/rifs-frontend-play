package forms.validation

import cats.data.ValidatedNel

case class FieldError(path: String, err: String)

case class FieldHint(path: String, hint: String)

trait FieldValidator[A, B] {
  outer =>

  /**
    * Provide a way for the validator to normalise the input value.
    */
  def normalise(a: A): A = a

  def validate(path: String, a: A): ValidatedNel[FieldError, B]

  def hintText(path: String, s: Option[String]): Option[FieldHint] = None

  def andThen[C](v2: FieldValidator[B, C]): FieldValidator[A, C] = new FieldValidator[A, C] {
    override def validate(path: String, a: A): ValidatedNel[FieldError, C] = outer.validate(path, a).andThen(v2.validate(path, _))

    override def hintText(path: String, s: Option[String]): Option[FieldHint] = outer.hintText(path, s) match {
      case None => v2.hintText(path, s)
      case h => h
    }
  }

}





