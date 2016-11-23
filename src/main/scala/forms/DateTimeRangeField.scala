package forms

import forms.validation.{FieldError, FieldHint}
import models.ApplicationSectionDetail
import play.api.libs.json.JsObject
import play.twirl.api.Html

case class DateTimeRangeField(name: String, allowPast: Boolean, isEndDateMandatory: Boolean) extends Field with DateTimeFormats {
  val startDateField = DateField(s"$name.startDate")
  val endDateField = DateField(s"$name.endDate")

  override def renderFormInput(app: ApplicationSectionDetail, answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html =
    views.html.renderers.dateTimeRangeField(this, app, answers, errs, hints)

  override def renderPreview(app: ApplicationSectionDetail, answers: JsObject): Html = ???

}
