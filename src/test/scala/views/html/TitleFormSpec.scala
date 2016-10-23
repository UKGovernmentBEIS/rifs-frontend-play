package views.html

import forms.{TextField, WordCountRule}
import models._
import org.jsoup.Jsoup
import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import play.api.libs.json.{JsObject, JsString}
import play.twirl.api.Html

class TitleFormSpec extends WordSpecLike with Matchers with OptionValues {

  "sectionForm" should {
    "show '20 words maximum' as help text if no value it present for the title" in {
      val html: Html = generatePage(None)

      val soup = Jsoup.parse(html.toString())
      Option(soup.getElementById("title_help_text")).value.text() shouldBe "20 words maximum"
    }

    "show 'Words remaining: 10' as help text when a 10-word value is supplied" in {
      val html = generatePage(Some(JsObject(Seq("title" -> JsString("one two three four five six seven eight nine ten")))))

      val soup = Jsoup.parse(html.toString())
      Option(soup.getElementById("title_help_text")).value.text() shouldBe "Words remaining: 10"
    }

    "show '2 words over limit' as help text when a 22-word value is supplied" in {
      val s = "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22"
      val html = generatePage(Some(JsObject(Seq("title" -> JsString(s)))))

      val soup = Jsoup.parse(html.toString())
      Option(soup.getElementById("title_help_text")).value.text() shouldBe "2 words over limit"
    }
  }

  def generatePage(values: Option[JsObject]) = {
    val section = values.map(vs => ApplicationSection(ApplicationSectionId(1), ApplicationId(1), 1, vs, None))

    sectionForm(
      Application(ApplicationId(1), ApplicationFormId(1)),
      section,
      ApplicationFormSection(1, "Event Title"),
      Opportunity(OpportunityId(1), "Research priorities in health care", "", None, OpportunityValue(0, ""), Seq()),
      Seq(TextField(Some("label"), "title", Seq(WordCountRule(20)), None, None).withValuesFrom(values.getOrElse(JsObject(Seq()))))
    )
  }
}
