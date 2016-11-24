package forms
import controllers.FieldCheck
import forms.validation.{FieldError, FieldHint}
import models.ApplicationSectionDetail
import play.api.libs.json.JsObject
import play.twirl.api.Html

case class TimeField(name: String) extends Field {
  override def check: FieldCheck = ???

  override def renderFormInput(app: ApplicationSectionDetail, answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html = ???

  override def renderPreview(app: ApplicationSectionDetail, answers: JsObject): Html = ???
}
