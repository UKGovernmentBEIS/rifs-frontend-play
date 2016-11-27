package forms
import controllers.FieldCheck
import forms.validation.{FieldError, FieldHint}
import models.{ApplicationSectionDetail, Question}
import play.api.libs.json.JsObject
import play.twirl.api.Html

case class TimeField(name: String) extends Field {
  override def check: FieldCheck = ???

  override def renderFormInput(questions: Map[String, Question], answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]) = ???

  override def renderPreview(questions: Map[String, Question], answers: JsObject) = ???
}
