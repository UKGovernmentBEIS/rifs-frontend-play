package views.html

import forms.WordCountRule
import models._
import org.jsoup.Jsoup
import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import play.twirl.api.Html

class TitleFormSpec extends WordSpecLike with Matchers with OptionValues {

  "titleForm" should {
    "show '20 words maximum' as help text if no value it present for the title" in {
      val html: Html = titleForm(
        Application(ApplicationId(1), ApplicationFormId(1)),
        None,
        ApplicationFormSection(1, "Event Title"),
        Opportunity(OpportunityId(1), "Research priorities in health care", "", None, OpportunityValue(0, ""), Seq()),
        Map("title" -> Seq(WordCountRule(20))),
        Map()
      )

      val soup = Jsoup.parse(html.toString())
      Option(soup.getElementById("title_help_text")).value.text() shouldBe "20 words maximum"
    }
  }
}
