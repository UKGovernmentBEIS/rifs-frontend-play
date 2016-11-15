package forms

import controllers.JsonHelpers
import forms.validation.{FieldError, FieldHint}
import models.{ApplicationFormSection, ApplicationOverview, Question}
import play.api.Logger
import play.api.libs.json.JsObject
import play.twirl.api.Html

case class DateValues(day: Option[String], month: Option[String], year: Option[String])

case class DateField(name: String) extends Field {
  override def renderFormInput(app: ApplicationOverview, formSection: ApplicationFormSection, questions: Map[String, Question], answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html = {
    Logger.debug(s"rendering date field with $name using $answers and $errs")
    views.html.renderers.dateField(this, questions, JsonHelpers.flatten(answers), errs)
  }

  override def renderPreview(app: ApplicationOverview, formSection: ApplicationFormSection, answers: JsObject): Html =
    views.html.renderers.preview.dateField(this, JsonHelpers.flatten(answers))
}
