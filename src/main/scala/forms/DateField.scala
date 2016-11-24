package forms

import controllers.{FieldCheck, FieldChecks, JsonHelpers}
import forms.validation.{DateFieldValidator, FieldError, FieldHint}
import models.ApplicationSectionDetail
import play.api.libs.json.{JsObject, Json}
import play.twirl.api.Html

case class DateValues(day: Option[String], month: Option[String], year: Option[String])

case class DateField(name: String, allowPast: Boolean) extends Field with DateTimeFormats {
  implicit val dvReads = Json.reads[DateValues]

  override val check: FieldCheck = FieldChecks.fromValidator(DateFieldValidator(allowPast))

  override def renderFormInput(app: ApplicationSectionDetail, answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html = {
    views.html.renderers.dateField(this, app.formSection.questionMap, JsonHelpers.flatten(answers), errs)
  }

  override def renderPreview(app: ApplicationSectionDetail, answers: JsObject): Html =
    views.html.renderers.preview.dateField(this, JsonHelpers.flatten(answers))

}

