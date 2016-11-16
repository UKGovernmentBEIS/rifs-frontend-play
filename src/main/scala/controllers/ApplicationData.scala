package controllers

import forms._
import forms.validation._
import models.Question
import play.api.libs.json._

object ApplicationData {

  sealed trait SectionType

  case object ItemSection extends SectionType

  case object VanillaSection extends SectionType

  import FieldChecks._

  implicit val dvReads = Json.reads[DateValues]
  implicit val dwdReads = Json.reads[DateWithDaysValues]
  implicit val civReads = Json.reads[CostItemValues]
  implicit val ciReads = Json.reads[CostItem]

  private val provisionalDateValidator: DateWithDaysValidator = DateWithDaysValidator(allowPast = false, 1, 9)

  def sectionTypeFor(sectionNumber: Int): SectionType = sectionNumber match {
    case 6 => ItemSection
    case _ => VanillaSection
  }

  def checksFor(sectionNumber: Int): Map[String, FieldCheck] = sectionNumber match {
      case 1 => Map("title" -> mandatoryText(20))
      case 2 => Map("provisionalDate" -> fromValidator(provisionalDateValidator))
      case 3 => Map("eventObjectives" -> mandatoryText(500))
      case 4 => Map("topicAndSpeaker" -> mandatoryText(500))
      case 5 => Map("eventAudience" -> mandatoryText(500))
      case 6 => Map("items" -> fromValidator(CostSectionValidator(2000)))
      case _ => Map()
    }

  def itemChecksFor(sectionNumber:Int): Map[String, FieldCheck] = sectionNumber match {
    case 6 => Map("item" -> fromValidator(CostItemValidator))
    case _ => Map()
  }

  def previewChecksFor(sectionNumber: Int): Map[String, FieldCheck] = sectionNumber match {
    case 1 => Map("title" -> mandatoryCheck)
    case 2 => Map("provisionalDate" -> fromValidator(provisionalDateValidator))
    case 3 => Map("eventObjectives" -> mandatoryCheck)
    case 4 => Map("topicAndSpeaker" -> mandatoryCheck)
    case 5 => Map("eventAudience" -> mandatoryCheck)
    case _ => Map()
  }

  val eventObjFormFields: Seq[Field] = Seq(TextAreaField(None, "eventObjectives"))

  val topicAndSpeakerFields: Seq[Field] = Seq(TextAreaField(None, "topicAndSpeaker"))

  val eventAudienceFields: Seq[Field] = Seq(TextAreaField(None, "eventAudience"))

  val titleFormFields: Seq[Field] = Seq(TextField(None, "title", isNumeric = false))
  val dateFormFields: Seq[Field] = Seq(DateWithDaysField("provisionalDate", provisionalDateValidator))

  def fieldsFor(sectionNum: Int): Option[Seq[Field]] = sectionNum match {
    case 1 => Some(titleFormFields)
    case 2 => Some(dateFormFields)
    case 3 => Some(eventObjFormFields)
    case 4 => Some(topicAndSpeakerFields)
    case 5 => Some(eventAudienceFields)
    case 6 => Some(Seq(CostListField("")))
    case _ => None
  }

  def itemFieldsFor(sectionNum:Int): Option[Seq[Field]] = sectionNum match {
    case 6 => Some(Seq(CostItemField("item")))
    case _ => None
  }
}
