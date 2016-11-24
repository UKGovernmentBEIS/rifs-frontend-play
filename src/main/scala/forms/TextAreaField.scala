package forms

import controllers.{FieldCheck, FieldChecks, JsonHelpers}
import forms.validation.{FieldError, FieldHint}
import models._
import play.api.libs.json.JsObject
import play.twirl.api.Html

case class TextAreaField(label: Option[String], name: String, maxWords: Int) extends Field {

  override val check: FieldCheck = FieldChecks.mandatoryText(maxWords)

  override def previewCheck: FieldCheck = FieldChecks.mandatoryCheck

  override def renderPreview(app: ApplicationSectionDetail, answers: JsObject): Html =
    views.html.renderers.preview.textAreaField(this, JsonHelpers.flatten(answers))

  override def renderFormInput(app: ApplicationSectionDetail, answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html =
    views.html.renderers.textAreaField(this, app.formSection.questionMap, JsonHelpers.flatten(answers), errs, hints)
}
