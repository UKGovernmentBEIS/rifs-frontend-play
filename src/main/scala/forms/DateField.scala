package forms

import controllers.JsonHelpers
import forms.validation.{FieldError, FieldHint}
import models.ApplicationSectionDetail
import play.api.libs.json.JsObject
import play.twirl.api.Html

case class DateValues(day: Option[String], month: Option[String], year: Option[String])

case class DateField(name: String) extends Field with DateTimeFormats {

  override def renderFormInput(app: ApplicationSectionDetail, answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html = {
    views.html.renderers.dateField(this, app.formSection.questionMap, JsonHelpers.flatten(answers), errs)
  }

  override def renderPreview(app: ApplicationSectionDetail, answers: JsObject): Html =
    views.html.renderers.preview.dateField(this, JsonHelpers.flatten(answers))
}

