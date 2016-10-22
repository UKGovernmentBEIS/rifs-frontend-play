package forms

import cats.data.NonEmptyList
import play.twirl.api.Html

trait Field {
  def renderFormInput(value: Option[String], errs: Option[NonEmptyList[String]]): Html

  def rules: Seq[FieldRule]

  def name: String
}


case class TextField(label: String, name: String, rules: Seq[FieldRule]) extends Field {
  override def renderFormInput(value: Option[String], errs: Option[NonEmptyList[String]]): Html = {
    views.html.renderers.textField(this, value, errs)
  }
}