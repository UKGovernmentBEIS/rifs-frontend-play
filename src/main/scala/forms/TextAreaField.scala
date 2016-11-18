package forms

import controllers.JsonHelpers
import forms.validation.{FieldError, FieldHint}
import models.{ApplicationDetail, ApplicationFormSection, ApplicationOverview, Question}
import play.api.libs.json.JsObject
import play.twirl.api.Html

case class TextAreaField(label: Option[String], name: String) extends Field {

  override def renderPreview(app: ApplicationDetail, formSection: ApplicationFormSection, answers: JsObject): Html =
    views.html.renderers.preview.textAreaField(this, JsonHelpers.flatten(answers))

  override def renderFormInput(app: ApplicationDetail, formSection: ApplicationFormSection, answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html =
    views.html.renderers.textAreaField(this, formSection.questionMap, JsonHelpers.flatten(answers), errs, hints)
}
