package forms.validation

import cats.data.ValidatedNel

case class FieldError(path: String, err: String)

trait FieldValidator[A, B] {
  outer =>

  /**
    * Provide a way for the validator to normalise the input value.
    */
  def normalise(a: A): A = a

  def validate(path: String, a: A): ValidatedNel[FieldError, B]

  def hintText(a: A): Option[String] = None

  def andThen[C](v2: FieldValidator[B, C]): FieldValidator[A, C] = new FieldValidator[A, C] {
    override def validate(path: String, a: A): ValidatedNel[FieldError, C] = outer.validate(path, a).andThen(v2.validate(path, _))
  }

}





