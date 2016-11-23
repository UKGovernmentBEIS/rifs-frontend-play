package forms

import controllers.JsonHelpers
import forms.validation.{FieldError, FieldHint}
import models._
import play.api.libs.json.JsObject
import play.twirl.api.Html

case class CostItemField(name: String) extends Field {

  val itemNameField = TextField(Some("Item"), s"$name.itemName", isNumeric = false)
  val costField = CurrencyField(Some("Cost"), s"$name.cost")
  val justificationField = TextAreaField(Some("Justification of item"), s"$name.justification")

  override def renderFormInput(app: ApplicationSectionDetail, answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html =
    views.html.renderers.costItemField(this, app.formSection.questionMap, JsonHelpers.flatten(answers), errs, hints)

  override def renderPreview(app: ApplicationSectionDetail, answers: JsObject): Html = Html("")
}
