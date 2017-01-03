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

package views.html

import eu.timepit.refined.auto._
import eu.timepit.refined.numeric._
import forms.TextAreaField
import forms.validation.FieldHint
import models._
import org.joda.time.LocalDate
import org.jsoup.Jsoup
import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import play.api.libs.json.JsObject
import play.twirl.api.Html

class EventAudienceFormSpec extends WordSpecLike with Matchers with OptionValues {

  "event audience form" should {
    "show '500 words maximum' as help text if no value it present for the title" in {
      val html: Html = generatePage(None)
      val soup = Jsoup.parse(html.toString())
      Option(soup.getElementById("eventAudience_hint_text")).value.text() shouldBe "500 words maximum"
    }
    "Event Audience Help text" in {
      val html: Html = generatePage(None)
      val soup = Jsoup.parse(html.toString())
      Option(soup.select("label.question")).value.text() shouldBe "Who is the event's target audience?"
    }
  }

  def generatePage(values: Option[JsObject]): Html = {
    val section = values.map(vs => ApplicationSection(AppSectionNumber(3), vs, None))
    val name = "eventAudience"
    val q = ApplicationFormQuestion(name, "Who is the event's target audience?", None, Option("Help Text"))
    val fs: ApplicationFormSection = ApplicationFormSection(AppSectionNumber(5), "Event Audience", Seq(q), SectionTypeForm, Seq(TextAreaField(Some("label"), name, 200)))

    val app = ApplicationSectionDetail(
      ApplicationId(1L),
      1,
      1,
      OpportunitySummary(OpportunityId(1L), "Research priorities in health care", new LocalDate(), None, OpportunityValue(0, ""), None, None),
      fs,
      None)

    sectionForm(
      app,
      JsObject(List.empty),
      List(),
      List(FieldHint(name, "500 words maximum"))
    )
  }
}
