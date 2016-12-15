package forms.validation

import cats.data.ValidatedNel
import play.api.libs.json.JsValue
import shapeless.tag._

case class FieldError(path: String, err: String)

case class FieldHint(path: String, hint: String, matchingJsType: Option[String] = None, matchingJsConfig: Option[String] = None)

trait FieldValidator[A, B] {
  outer =>

  sealed trait NormalisedTag

  type Normalised[T] = T @@ NormalisedTag

  /**
    * Provide a way for the validator to normalise the input value. To keep things simple for the validator
    * implementation this returns an `A` rather than a `Normalised[A]` and the `validate` method does the
    * tagging.
    */
  def normalise(a: A): A = a

  /**
    * Sometimes the type tag confuses the compiler, usually in `match` expressions. This
    * convenience function strips the tag from the normalised value for those situations.
    */
  final def denormal(a: Normalised[A]): A = a

  private def normal(a: A): Normalised[A] = a.asInstanceOf[A @@ NormalisedTag]

  final def validate(path: String, a: A): ValidatedNel[FieldError, B] = doValidation(path, normal(normalise(a)))

  protected def doValidation(path: String, a: Normalised[A]): ValidatedNel[FieldError, B]

  def hintText(path: String, jv: JsValue): List[FieldHint] = List()

  def andThen[C](v2: FieldValidator[B, C]): FieldValidator[A, C] = new FieldValidator[A, C] {
    override def doValidation(path: String, a: Normalised[A]): ValidatedNel[FieldError, C] = outer.validate(path, a).andThen(v2.validate(path, _))

    override def hintText(path: String, jv: JsValue): List[FieldHint] = v2.hintText(path, jv) ++ outer.hintText(path, jv)
  }

}





