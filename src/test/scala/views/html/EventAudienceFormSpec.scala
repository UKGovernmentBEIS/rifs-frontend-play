package views.html

import forms.{TextAreaField, WordCountRule}
import models._
import org.jsoup.Jsoup
import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import play.api.libs.json.JsObject
import play.twirl.api.Html

class EventAudienceFormSpec extends WordSpecLike with Matchers with OptionValues {

  "sectionForm" should {

    "show '500 words maximum' as help text if no value it present for the title" in {
      val html: Html = generatePage(None)
      val soup = Jsoup.parse(html.toString())
      Option(soup.getElementById("title_help_text")).value.text() shouldBe "500 words maximum"
    }
    "Event Audience Help text" in {
      val html: Html = generatePage(None)
      val soup = Jsoup.parse(html.toString())
      Option(soup.getElementsByTag("h4")).value.text() shouldBe "Who is the event's target audience?"
    }
  }

  def generatePage(values: Option[JsObject]) = {
    val section = values.map(vs => ApplicationSection(ApplicationSectionId(1), ApplicationId(1), 3, vs, None))

    sectionForm(
      Application(ApplicationId(1), ApplicationFormId(1)),
      section,
      ApplicationForm(ApplicationFormId(1), OpportunityId(1), Seq(ApplicationFormSection(5, "Event Audience"))),
      ApplicationFormSection(5, "Event Audience"),
      Opportunity(OpportunityId(1), "Research priorities in health care", "", None, OpportunityValue(0, ""), Seq()),
      Seq(TextAreaField(Some("label"), "eventAudience", Seq(WordCountRule(500)), None, None, Option(Question("Who is the event's target audience?", Option(""), Option("Help Text")))).withValuesFrom(values.getOrElse(JsObject(Seq()))))
    )
  }
}
