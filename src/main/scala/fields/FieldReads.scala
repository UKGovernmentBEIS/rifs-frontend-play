package fields

import forms._
import play.api.libs.json._

case class FieldType(`type`: String)

object FieldReads {

  implicit val fieldTypeReads = Json.reads[FieldType]
  implicit val textFieldReads = Json.reads[TextField]
  implicit val textAreaFieldReads = Json.reads[TextAreaField]
  implicit val dateWithDaysReads = Json.reads[DateWithDaysField]
  implicit val dateTimeRangeReads = Json.reads[DateTimeRangeField]
  implicit val costListReads = Json.reads[CostListField]
  implicit val costItemReads = Json.reads[CostItemField]

  implicit object fieldReads extends Reads[Field] {
    override def reads(json: JsValue): JsResult[Field] = {
      json.validate[FieldType].flatMap { o =>
        o.`type` match {
          case "text" => json.validate[TextField]
          case "textArea" => json.validate[TextAreaField]
          case "dateWithDays" => json.validate[DateWithDaysField]
          case "dateTimeRange" => json.validate[DateTimeRangeField]
          case "costList" => json.validate[CostListField]
          case "costItem" => json.validate[CostItemField]
          case t => JsError(s"unknown field type $t")
        }
      }
    }
  }

}
