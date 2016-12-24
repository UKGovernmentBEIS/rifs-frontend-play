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

package controllers

import forms.DateValues
import forms.validation.DateTimeRangeValues
import play.api.libs.json._

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

  implicit def OptionReads[T: Reads] = new Reads[Option[T]] {
    override def reads(json: JsValue): JsResult[Option[T]] = json match {
      case JsNull => JsSuccess(None)
      case j => j.validate[T].map(Some(_))
    }
  }
}
