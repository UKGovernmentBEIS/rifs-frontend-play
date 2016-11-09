package forms

import forms.validation.{FieldError, FieldHint}
import models.Question
import play.twirl.api.Html

case class CostItemField(name: String) extends Field {

  val itemNameField = TextField(Some("Item"), s"$name.itemName", isNumeric = false)
  val costField = CurrencyField(Some("Cost"), s"$name.cost")
  val justificationField = TextAreaField(Some("Justification of item"), s"$name.justification")

  override def renderFormInput(questions: Map[String, Question], answers: Map[String, String], errs: Seq[FieldError], hints: Seq[FieldHint]): Html =
    views.html.renderers.costItemField(this, questions, answers, errs, hints)


  override def renderPreview(answers: Map[String, String]): Html = Html("<html><h1>The costs preview has not been implemented yet</h1></html>")
}
