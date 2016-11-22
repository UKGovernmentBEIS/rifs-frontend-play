package forms

import forms.validation.{FieldError, FieldHint}
import models._
import play.api.libs.json.JsObject
import play.twirl.api.Html

trait Field {
  def renderFormInput(app: ApplicationDetail, formSection: ApplicationFormSection, answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html

  def renderPreview(app: ApplicationDetail, formSection: ApplicationFormSection, answers: JsObject): Html

  def name: String

}

