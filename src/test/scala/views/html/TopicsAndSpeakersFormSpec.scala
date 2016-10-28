package views.html

import forms.{TextAreaField, WordCountRule}
import models._
import org.jsoup.Jsoup
import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import play.api.libs.json.JsObject
import play.twirl.api.Html

class TopicsAndSpeakersFormSpec extends WordSpecLike with Matchers with OptionValues {

  "sectionForm" should {

    "show '500 words maximum' as help text if no value it present for the Topics And Speakers" in {
      val html: Html = generatePage(None)
      val soup = Jsoup.parse(html.toString())
      Option(soup.getElementById("title_help_text")).value.text() shouldBe "500 words maximum"
    }
    "'Topics And Speakers' Help text" in {
      val html: Html = generatePage(None)
      val soup = Jsoup.parse(html.toString())
      Option(soup.getElementsByClass("question")).value.text() shouldBe "What topics do you intend to cover?"
    }
  }

  def generatePage(values: Option[JsObject]) = {
    val section = values.map(vs => ApplicationSection(ApplicationSectionId(1), ApplicationId(1), 4, vs, None))

    sectionForm(
      Application(ApplicationId(1), ApplicationFormId(1)),
      ApplicationOverview(ApplicationId(1), ApplicationFormId(1), Seq()),
      ApplicationForm(ApplicationFormId(1), OpportunityId(1), Seq(ApplicationFormSection(4, "Topics And Speakers"))),
      section,
      ApplicationFormSection(4, "Topics And Speakers"),
      Opportunity(OpportunityId(1), "Research priorities in health care", "", None, OpportunityValue(0, ""), Seq()),
      Seq(TextAreaField(Some("label"), "topicAndSpeakerRules", Seq(WordCountRule(500)), None, None, Option(Question("What topics do you intend to cover?", Option(""), Option("Help Text")))).withValuesFrom(values.getOrElse(JsObject(Seq()))))
    )
  }
}
