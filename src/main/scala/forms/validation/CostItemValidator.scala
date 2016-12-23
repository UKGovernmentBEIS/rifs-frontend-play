/*
 * Copyright (C) 2016  Department for Business, Energy and Industrial Strategy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package forms.validation

import cats.data.ValidatedNel
import cats.syntax.cartesian._
import cats.syntax.validated._
import forms.validation.FieldValidator.Normalised

case class CostItemValues(itemName: Option[String], cost: Option[String], justification: Option[String], itemNumber: Option[Int])

case class CostList(items: List[CostItem])

case class CostItem(itemName: String, cost: BigDecimal, justification: String, itemNumber: Option[Int] = None) {
  val costText: String = cost.setScale(2, BigDecimal.RoundingMode.HALF_UP).toString
}

case object CostItemValidator extends FieldValidator[CostItemValues, CostItem] {
  val itemValidator = MandatoryValidator(Some("item")).andThen(WordCountValidator(20))
  val costValidator = CurrencyValidator.anyValue
  val justificationValidator = MandatoryValidator(Some("justification")).andThen(WordCountValidator(200))

  override def doValidation(path: String, costItemValues: Normalised[CostItemValues]): ValidatedNel[FieldError, CostItem] = {
    val itemV = itemValidator.validate(s"$path.itemName", costItemValues.itemName)
    val costV = costValidator.validate(s"$path.cost", costItemValues.cost)
    val justV = justificationValidator.validate(s"$path.justification", costItemValues.justification)

    (itemV |@| costV |@| justV).map(CostItem.apply(_, _, _, None))
  }

  override def doHinting(path: String, costItemValues: Normalised[CostItemValues]): List[FieldHint] = {
    justificationValidator.hintText(s"$path.justification", costItemValues.justification)
  }
}

case class CostSectionValidator(maxValue: BigDecimal) extends FieldValidator[CostList, List[CostItem]] {
  val nonEmptyV = new FieldValidator[List[CostItem], List[CostItem]] {
    override def doValidation(path: String, items: Normalised[List[CostItem]]): ValidatedNel[FieldError, List[CostItem]] =
      if (items.isEmpty) FieldError(path, s"Must provide at least one item.").invalidNel
      else items.validNel
  }

  val notTooCostlyV = new FieldValidator[List[CostItem], List[CostItem]] {
    override def doValidation(path: String, items: Normalised[List[CostItem]]): ValidatedNel[FieldError, List[CostItem]] =
      if (items.map(_.cost).sum > maxValue) FieldError(path, s"Total requested exceeds limit. Please check costs of items.").invalidNel
      else items.validNel
  }

  override def doValidation(path: String, cvs: Normalised[CostList]): ValidatedNel[FieldError, List[CostItem]] = {
    nonEmptyV.andThen(notTooCostlyV).validate("", cvs.items)
  }
}