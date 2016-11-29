package controllers

import forms._
import forms.validation._
import play.api.libs.json._

object ApplicationData {

  sealed trait SectionType

  case object ItemSection extends SectionType

  case object VanillaSection extends SectionType

  import FieldChecks._

  implicit val civReads = Json.reads[CostItemValues]
  implicit val ciReads = Json.reads[CostItem]

  def itemChecksFor(sectionNumber: Int): Map[String, FieldCheck] = sectionNumber match {
    case 6 => Map("item" -> fromValidator(CostItemValidator))
    case _ => Map()
  }

  def itemFieldsFor(sectionNum: Int): Option[Seq[Field]] = sectionNum match {
    case 6 => Some(Seq(CostItemField("item")))
    case _ => None
  }
}
