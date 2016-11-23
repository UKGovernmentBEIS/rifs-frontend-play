package forms

import controllers.ApplicationData._
import forms.validation.{CostItem, FieldError, FieldHint}
import models._
import play.api.libs.json.{JsArray, JsObject, JsValue}
import play.twirl.api.Html

case class CostListField(name: String) extends Field {
  override def renderFormInput(app: ApplicationSectionDetail, answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html = {
    val itemValues: Seq[JsValue] = (answers \ "items").validate[JsArray].asOpt.map(_.value).getOrElse(Seq())
    val costItems = itemValues.flatMap(_.validate[CostItem].asOpt)
    views.html.renderers.costListField(this, app, costItems, errs)
  }

  override def renderPreview(app: ApplicationSectionDetail, answers: JsObject): Html = {
    val itemValues: Seq[JsValue] = (answers \ "items").validate[JsArray].asOpt.map(_.value).getOrElse(Seq())
    val costItems = itemValues.flatMap(_.validate[CostItem].asOpt)
    views.html.renderers.preview.costListField(this, costItems)
  }
}
