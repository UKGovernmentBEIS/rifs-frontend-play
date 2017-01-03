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

import com.wellfactored.playbindings.ValueClassFormats
import controllers.RefinedBinders
import forms.validation._
import models._
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

package object services
  extends ValueClassFormats
    with RefinedBinders {

  implicit val jodaLocalDateTimeFormat = new Format[LocalDateTime] {
    val dtf = DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss")

    override def writes(o: LocalDateTime): JsValue = JsString(dtf.print(o))

    override def reads(json: JsValue): JsResult[LocalDateTime] =
      implicitly[Reads[JsString]].reads(json).flatMap { js =>
        Try(dtf.parseLocalDateTime(js.value)) match {
          case Success(s) => JsSuccess(s)
          case Failure(t) => JsError(t.getMessage)
        }
      }
  }

  implicit val fieldReads = fields.FieldReads.fieldReads

  private val dtPattern = "dd MMM yyyy HH:mm:ss"
  implicit val dtReads = Reads.jodaDateReads(dtPattern)
  implicit val dtWrites = Writes.jodaDateWrites(dtPattern)

  implicit val jldReads = Reads.jodaLocalDateReads("d MMM yyyy")
  implicit val jldWrites = Writes.jodaLocalDateWrites("d MMM yyyy")

  implicit val odsFmt = Json.format[OpportunityDescriptionSection]
  implicit val ovFmt = Json.format[OpportunityValue]
  implicit val odFmt = Json.format[OpportunityDuration]
  implicit val oppFmt = Json.format[Opportunity]
  implicit val oppSummaryFmt = Json.format[OpportunitySummary]

  implicit val appSectionReads = Json.reads[ApplicationSection]
  implicit val appReads = Json.reads[Application]
  implicit val appSecOvRead = Json.reads[ApplicationSectionOverview]
  implicit val appOvRead = Json.reads[ApplicationOverview]
  implicit val saRefReads = Json.reads[SubmittedApplicationRef]
  implicit val oppSecReads = Json.reads[OpportunityDescriptionSection]
  implicit val oppValueReads = Json.reads[OpportunityValue]
  implicit val oppDurReads = Json.reads[OpportunityDuration]
  implicit val oppSummaryReads = Json.reads[OpportunitySummary]
  implicit val oppReads = Json.reads[Opportunity]
  implicit val appFormQReads = Json.reads[ApplicationFormQuestion]
  implicit val appFormSecReads = Json.reads[ApplicationFormSection]
  implicit val appFormReads = Json.reads[ApplicationForm]
  implicit val appDetailReads = Json.reads[ApplicationDetail]
  implicit val appSecDetailReads = Json.reads[ApplicationSectionDetail]

  implicit val civReads = Json.reads[CostItemValues]
  implicit val ciReads = Json.reads[CostItem]
}
