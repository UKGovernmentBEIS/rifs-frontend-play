package views.html

import forms.TextAreaField
import forms.validation.FieldHint
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
      Option(soup.getElementById("topicAndSpeakerRules_hint_text")).value.text() shouldBe "500 words maximum"
    }
    "'Topics And Speakers' Help text" in {
      val html: Html = generatePage(None)
      val soup = Jsoup.parse(html.toString())
      Option(soup.select("p.question")).value.text() shouldBe "What topics do you intend to cover?"
    }
  }

  def generatePage(values: Option[JsObject]): Html = {
    val section = values.map(vs => ApplicationSection(ApplicationSectionId(1), ApplicationId(1), 3, vs, None))
    val q = Question("What topics do you intend to cover?", Option(""), Option("Help Text"))
    val name = "topicAndSpeakerRules"

    sectionForm(
      ApplicationOverview(ApplicationId(1), ApplicationFormId(1), Seq()),
      ApplicationForm(ApplicationFormId(1), OpportunityId(1), Seq(ApplicationFormSection(5, "Event Audience", Seq()))),
      section,
      ApplicationFormSection(3, "Event Objectives", Seq()),
      Opportunity(OpportunityId(1), "Research priorities in health care", "", None, OpportunityValue(0, ""), Seq()),
      Seq(TextAreaField(Some("label"), name)),
      Map(name -> q),
      JsObject(Seq()),
      List(),
      List(FieldHint(name, "500 words maximum"))
    )
  }
}
