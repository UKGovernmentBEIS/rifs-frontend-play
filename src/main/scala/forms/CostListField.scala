package forms

import controllers.ApplicationData._
import forms.validation.{CostItem, FieldError, FieldHint}
import models.{ApplicationDetail, ApplicationFormSection, ApplicationOverview, Question}
import play.api.libs.json.{JsArray, JsObject, JsValue}
import play.twirl.api.Html

case class CostListField(name: String) extends Field {
  override def renderFormInput(app: ApplicationDetail, formSection: ApplicationFormSection, answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html = {
    val itemValues: Seq[JsValue] = (answers \ "items").validate[JsArray].asOpt.map(_.value).getOrElse(Seq())
    val costItems = itemValues.flatMap(_.validate[CostItem].asOpt)
    views.html.renderers.costListField(this, app, formSection, costItems, errs)
  }

  override def renderPreview(app: ApplicationDetail, formSection: ApplicationFormSection, answers: JsObject): Html = {
    val itemValues: Seq[JsValue] = (answers \ "items").validate[JsArray].asOpt.map(_.value).getOrElse(Seq())
    val costItems = itemValues.flatMap(_.validate[CostItem].asOpt)
    views.html.renderers.preview.costListField(this, costItems)
  }
}
