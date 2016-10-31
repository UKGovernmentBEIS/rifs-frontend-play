package forms

import forms.validation.{FieldError, FieldHint}
import models.Question
import play.twirl.api.Html

case class CostItemField(name: String) extends Field {

  val itemNameField = TextField(Some("Item"), "itemName", isNumeric = false)
  val costField = CurrencyField(Some("Cost"), "cost")
  val percentageField = TextField(Some("% from RC"), "percentage", isNumeric = true)
  val justificationField = TextAreaField(Some("Justification of item"), "justification")

  override def renderFormInput(questions: Map[String, Question], answers: Map[String, String], errs: Seq[FieldError], hints: Seq[FieldHint]): Html =
    views.html.renderers.costItemField(this, questions, answers, errs, hints)


  override def renderPreview(answers: Map[String, String]): Html = ???
}
