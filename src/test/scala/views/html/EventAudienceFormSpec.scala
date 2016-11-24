package views.html

import forms.TextAreaField
import forms.validation.FieldHint
import models._
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
      Option(soup.select("p.question")).value.text() shouldBe "Who is the event's target audience?"
    }
  }

  def generatePage(values: Option[JsObject]): Html = {
    val section = values.map(vs => ApplicationSection(3, vs, None))
    val name = "eventAudience"
    val q = ApplicationFormQuestion(name, "Who is the event's target audience?", Option(""), Option("Help Text"))
    val fs: ApplicationFormSection = ApplicationFormSection(5, "Event Audience", Seq(q), Seq(TextAreaField(Some("label"), name, 200)))

    val app = ApplicationSectionDetail(
      ApplicationId(1),
      1,
      1,
      OpportunitySummary(OpportunityId(1), "Research priorities in health care", "", None, OpportunityValue(0, "")),
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
