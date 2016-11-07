package forms.validation

import cats.data.ValidatedNel
import play.api.libs.json.JsValue

case class FieldError(path: String, err: String)

case class FieldHint(path: String, hint: String, matchingJsType: Option[String] = None, matchingJsConfig: Option[String] = None)

trait FieldValidator[A, B] {
  outer =>

  /**
    * Provide a way for the validator to normalise the input value.
    */
  def normalise(a: A): A = a

  def validate(path: String, a: A): ValidatedNel[FieldError, B]

  def hintText(path: String, jv: JsValue): List[FieldHint] = List()

  def andThen[C](v2: FieldValidator[B, C]): FieldValidator[A, C] = new FieldValidator[A, C] {
    override def validate(path: String, a: A): ValidatedNel[FieldError, C] = outer.validate(path, a).andThen(v2.validate(path, _))

    override def hintText(path: String, jv: JsValue): List[FieldHint] = v2.hintText(path, jv) ++ outer.hintText(path, jv)
  }

}





