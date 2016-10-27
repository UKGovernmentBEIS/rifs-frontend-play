package forms

import play.api.libs.json.{JsObject, JsValue}


class DateWithDaysRule(dateName: String, dateRules: Seq[FieldRule], daysName: String, daysRules: Seq[FieldRule]) extends FieldRule {
  override def validateOnPreview: Boolean = true

  override def validate(value: JsValue): Seq[String] = {
    val o = value.validate[JsObject].asOpt.getOrElse(JsObject(Seq()))

    val dateValue =(o \ dateName).toOption.getOrElse(JsObject(Seq()))
    val daysValue = (o \ daysName).toOption.getOrElse(JsObject(Seq()))

    //dateRules.flatMap()

    ???
  }

  override def helpText(value: JsValue): Option[String] = None
}
