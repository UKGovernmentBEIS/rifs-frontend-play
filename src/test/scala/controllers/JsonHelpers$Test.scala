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

import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import play.api.libs.json.{JsArray, JsObject, JsString}

import scala.collection.mutable.ArrayBuffer

class JsonHelpers$Test extends WordSpecLike with Matchers with OptionValues {
  val input: Map[String, Seq[String]] = Map(
    "provisionalDate.date.day" -> ArrayBuffer("2"),
    "provisionalDate.date.month" -> ArrayBuffer("5"),
    "provisionalDate.date.year" -> ArrayBuffer("2017"),
    "provisionalDate.days" -> ArrayBuffer("4"))

  "formToJson" should {
    "convert to correct JsObject" in {
      val o = JsonHelpers.formToJson(input)

      (o \ "provisionalDate" \ "days").toOption.value shouldBe JsString("4")
      (o \ "provisionalDate" \ "date" \ "day").toOption.value shouldBe JsString("2")
    }
  }

  "allFieldsEmpty" should {
    "consider an empty items list as an empty field" in {
      val o = JsObject(Seq("items" -> JsArray(Seq())))

      JsonHelpers.allFieldsEmpty(o) shouldBe true
    }
  }
}
