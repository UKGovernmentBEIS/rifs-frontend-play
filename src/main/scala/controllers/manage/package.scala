package controllers

import forms.DateValues
import forms.validation.DateTimeRangeValues
import play.api.libs.json.Json

package object manage {
  implicit val dvFmt = Json.format[DateValues]
  implicit val dtrFmt = Json.format[DateTimeRangeValues]
  val PREVIEW_BACK_URL_FLASH = "PREVIEW_BACK_URL_FLASH"
}
