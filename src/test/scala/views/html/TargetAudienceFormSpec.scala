package views.html

import forms.{TextAreaField, WordCountRule}
import models._
import org.jsoup.Jsoup
import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import play.api.libs.json.JsObject
import play.twirl.api.Html

class TargetAudienceFormSpec extends WordSpecLike with Matchers with OptionValues {

  "sectionForm" should {

    "show '500 words maximum' as help text if no value it present for the title" in {
      val html: Html = generatePage(None)
      val soup = Jsoup.parse(html.toString())
      Option(soup.getElementById("title_help_text")).value.text() shouldBe "500 words maximum"
    }
    "Event Objective Help text" in {
      val html: Html = generatePage(None)
      val soup = Jsoup.parse(html.toString())
      Option(soup.getElementsByTag("h4")).value.text() shouldBe "What are the objectives of the event?"
    }
  }

  def generatePage(values: Option[JsObject]) = {
    val section = values.map(vs => ApplicationSection(ApplicationSectionId(1), ApplicationId(1), 3, vs, None))

    sectionForm(
      Application(ApplicationId(1), ApplicationFormId(1)),
      section,
      ApplicationFormSection(3, "Event Objectives"),
      Opportunity(OpportunityId(1), "Research priorities in health care", "", None, OpportunityValue(0, ""), Seq()),
      Seq(TextAreaField(Some("label"), "eventObjectives", Seq(WordCountRule(500)), None, None, Option(Question("What are the objectives of the event?", Option(""), Option("Help Text")))).withValuesFrom(values.getOrElse(JsObject(Seq()))))
    )
  }
}
