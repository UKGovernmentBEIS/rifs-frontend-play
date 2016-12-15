package controllers

import forms.DateValues
import forms.validation.DateTimeRangeValues
import play.api.libs.json.Json

package object manage {
  implicit val dvFmt = Json.format[DateValues]
  implicit val dtrFmt = Json.format[DateTimeRangeValues]
  val PREVIEW_BACK_URL_FLASH = "PREVIEW_BACK_URL_FLASH"

  sealed trait CreateOpportunityChoice {
    def name: String
  }

  object CreateOpportunityChoice {
    def apply(s: Option[String]): Option[CreateOpportunityChoice] = s match {
      case Some(NewOpportunityChoice.name) => Some(NewOpportunityChoice)
      case Some(ReuseOpportunityChoice.name) => Some(ReuseOpportunityChoice)
      case _ => None
    }
  }

  case object NewOpportunityChoice extends CreateOpportunityChoice {
    val name = "new"
  }

  case object ReuseOpportunityChoice extends CreateOpportunityChoice {
    val name = "reuse"
  }
}
