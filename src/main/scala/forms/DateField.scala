package forms

import controllers.JsonHelpers
import forms.validation.{FieldError, FieldHint}
import models.{ApplicationDetail, ApplicationFormSection, ApplicationOverview, Question}
import play.api.Logger
import play.api.libs.json.JsObject
import play.twirl.api.Html

case class DateValues(day: Option[String], month: Option[String], year: Option[String])

case class DateField(name: String) extends Field {
  override def renderFormInput(app: ApplicationDetail, formSection: ApplicationFormSection, answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html = {
    views.html.renderers.dateField(this, formSection.questionMap, JsonHelpers.flatten(answers), errs)
  }

  override def renderPreview(app: ApplicationDetail, formSection: ApplicationFormSection, answers: JsObject): Html =
    views.html.renderers.preview.dateField(this, JsonHelpers.flatten(answers))
}
