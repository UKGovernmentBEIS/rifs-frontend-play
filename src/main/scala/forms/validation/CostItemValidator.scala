package forms.validation

import cats.data.ValidatedNel

case class CostItemValues(itemName: Option[String], cost: Option[String], percentage: Option[String], justification: Option[String])

case class CostItem(item: String, cost: BigDecimal, percentage: Int, justification: String)

case object CostItemValidator extends FieldValidator[CostItemValues, CostItem] {
  val itemValidator = MandatoryValidator(Some("item")).andThen(WordCountValidator(20))
  val costValidator = CurrencyValidator
  val percentageValidator = IntValidator(1, 100)
  val justificationValidator = MandatoryValidator(Some("justification")).andThen(WordCountValidator(200))

  override def validate(path: String, a: CostItemValues): ValidatedNel[FieldError, CostItem] = {
    val itemV = itemValidator.validate(s"$path.itemName", a.itemName)
    val costV = costValidator.validate(s"$path.cost", a.cost)
    val percentageV = percentageValidator.validate(s"$path.percentage", a.percentage.getOrElse(""))
    val justV = justificationValidator.validate(s"$path.justification", a.justification)

    // IDEA doesn't think this import is used - take care not to optimise it away! I've duplicated
    // it in a comment so it's easy to restore if you lose it by mistake.
    //import cats.syntax.cartesian._
    import cats.syntax.cartesian._
    (itemV |@| costV |@| percentageV |@| justV).map(CostItem.apply _)
  }
}
