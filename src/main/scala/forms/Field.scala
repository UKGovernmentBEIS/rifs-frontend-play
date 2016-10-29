package forms

import forms.validation.{FieldError, FieldHint}
import play.api.libs.json._
import play.twirl.api.Html

trait Field {
  def renderFormInput(questions: Map[String, String], answers: Map[String, String], errs: Seq[FieldError], hints: Seq[FieldHint]): Html

  def renderPreview(answers: Map[String, String]): Html

  def name: String

  protected def stringValue(o: JsObject, n: String): Option[String] = (o \ n).validate[JsString].asOpt.map(_.value)

  protected def objectValue(o: JsObject, n: String): Option[JsObject] = (o \ n).validate[JsObject].asOpt
}

