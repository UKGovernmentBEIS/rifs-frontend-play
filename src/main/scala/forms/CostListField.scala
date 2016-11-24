package forms

import controllers.{FieldCheck, FieldChecks}
import forms.validation.{CostItem, CostSectionValidator, FieldError, FieldHint}
import models._
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import play.twirl.api.Html

case class CostListField(name: String) extends Field {
  implicit val ciReads = Json.reads[CostItem]

  // TODO: Remove hard-coded 2000
  override val check: FieldCheck = FieldChecks.fromValidator(CostSectionValidator(2000))

  override def previewCheck: FieldCheck = FieldChecks.noCheck

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
