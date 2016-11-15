package forms

import controllers.ApplicationData._
import forms.validation.{CostItem, FieldError, FieldHint}
import models.{ApplicationFormSection, ApplicationOverview, Question}
import play.api.Logger
import play.api.libs.json.{JsArray, JsObject, JsValue}
import play.twirl.api.Html

case class CostListField(name: String) extends Field {
  override def renderFormInput(app: ApplicationOverview, formSection: ApplicationFormSection, questions: Map[String, Question], answers: JsObject, errs: Seq[FieldError], hints: Seq[FieldHint]): Html = {
    Logger.debug(s"cost answers are $answers")
    val itemValues: Seq[JsValue] = (answers \ "items").validate[JsArray].asOpt.map(_.value).getOrElse(Seq())
    Logger.debug(s"itemValues are $itemValues")
    val costItems = itemValues.flatMap(_.validate[CostItem].asOpt)
    Logger.debug(s"costItems are $costItems")
    views.html.renderers.costListField(this, app, formSection,questions, costItems, errs)
  }

  override def renderPreview(app: ApplicationOverview, formSection: ApplicationFormSection, answers: JsObject): Html = ???
}
