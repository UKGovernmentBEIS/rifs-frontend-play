package forms

import controllers.FieldCheck
import forms.validation.{FieldError, FieldHint}
import models._
import play.api.libs.json.JsObject
import play.twirl.api.Html

trait Field {
  def renderFormInput(app: ApplicationSectionDetail, answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html

  def renderPreview(app: ApplicationSectionDetail, answers: JsObject): Html

  def name: String

  def check: FieldCheck

  /**
    * Allow for more relaxed checks on preview if needed
    */
  def previewCheck: FieldCheck = check

}

