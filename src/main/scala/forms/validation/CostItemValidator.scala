package forms.validation

import cats.data.ValidatedNel
import cats.implicits._
import play.api.libs.json.{JsString, JsValue}

case class CostItemValues(itemName: Option[String], cost: Option[String], justification: Option[String], itemNumber: Option[Int])

case class CostItem(itemName: String, cost: BigDecimal, justification: String, itemNumber: Option[Int] = None)

case object CostItemValidator extends FieldValidator[CostItemValues, CostItem] {
  val itemValidator = MandatoryValidator(Some("item")).andThen(WordCountValidator(20))
  val costValidator = CurrencyValidator
  val justificationValidator = MandatoryValidator(Some("justification")).andThen(WordCountValidator(200))

  override def validate(path: String, a: CostItemValues): ValidatedNel[FieldError, CostItem] = {
    val itemV = itemValidator.validate(s"$path.itemName", a.itemName)
    val costV = costValidator.validate(s"$path.cost", a.cost)
    val justV = justificationValidator.validate(s"$path.justification", a.justification)

    (itemV |@| costV |@| justV).map(CostItem.apply(_, _, _, None))
  }

  override def hintText(path: String, jv: JsValue): List[FieldHint] = {
    val just = (jv \ "justification").validate[JsString].asOpt.getOrElse(JsString(""))
    justificationValidator.hintText(s"$path.justification", just)
  }

}

case class CostSectionValidator(maxValue: BigDecimal) extends FieldValidator[List[CostItemValues], List[CostItem]] {
  override def validate(path: String, itemValues: List[CostItemValues]): ValidatedNel[FieldError, List[CostItem]] = {
    itemValues.map(CostItemValidator.validate(path, _)).sequenceU.andThen { items: List[CostItem] =>
      if (items.map(_.cost).sum > maxValue) FieldError(path, s"Total cost exceeds the maximum of $maxValue").invalidNel
      else items.validNel
    }
  }
}