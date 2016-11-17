package forms

import forms.validation.{FieldError, FieldHint}
import models.{ApplicationFormSection, ApplicationOverview, Question}
import play.api.libs.json.JsObject
import play.twirl.api.Html

trait Field {
  def renderFormInput(app: ApplicationOverview, formSection: ApplicationFormSection, questions: Map[String, Question], answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html

  def renderPreview(app: ApplicationOverview, formSection: ApplicationFormSection, answers: JsObject): Html

  def name: String
}

