package forms.validation

import cats.data.ValidatedNel

trait FieldValidator[A, B] {
  outer =>

  /**
    * Provide a way for the validator to normalise the input value.
    */
  def normalise(a: A): A = a

  def validate(a: A): ValidatedNel[String, B]

  def hintText(a: A): Option[String] = None

  def andThen[C](v2: FieldValidator[B, C]): FieldValidator[A, C] = new FieldValidator[A, C] {
    override def validate(a: A): ValidatedNel[String, C] = outer.validate(a).andThen(v2.validate)
  }

}





