package models

import org.joda.time.{DateTime, LocalDate}
import org.scalatest.{Matchers, OptionValues, WordSpecLike}

class OpertunityTest  extends WordSpecLike with Matchers with OptionValues {
  val testDateBeforeCurrent = LocalDate.now.minusMonths(1)
  val testDateAfterCurrent = LocalDate.now.plusMonths(1)
  val testDateCurrent = LocalDate.now
  val publishedDate: DateTime = DateTime.now.minusMonths(1)

  "validate" when {
    "Opportunity is published and starts before current date " should {
      "show as status Open" in {
        val o: Opportunity = Opportunity (OpportunityId(1), "Test opportunity", testDateBeforeCurrent, None, OpportunityValue(2000.00, "spondulix"), Some(publishedDate), None, Seq())
        o.statusString shouldBe "Open"
      }
    }

    "Opportunity is published and starts on current date " should {
      "show as status Open" in {
        val o: Opportunity = Opportunity (OpportunityId(1), "Test opportunity", testDateCurrent, None, OpportunityValue(2000.00, "spondulix"), Some(publishedDate), None, Seq())
        o.statusString shouldBe "Open"
      }
    }

    "Opportunity is published and starts before current date and ends before end date" should {
      "show as status Open" in {
        val o: Opportunity = Opportunity (OpportunityId(1), "Test opportunity", testDateBeforeCurrent, Some(testDateAfterCurrent), OpportunityValue(2000.00, "spondulix"), Some(publishedDate), None, Seq())
        o.statusString shouldBe "Open"
      }
    }

    "Opportunity is published and starts after current date" should {
      "show as status Queued" in {
        val o: Opportunity = Opportunity(OpportunityId(1), "Test opportunity", testDateAfterCurrent, None, OpportunityValue(2000.00, "spondulix"), Some(publishedDate), None, Seq())
        o.statusString shouldBe "Queued"
      }
    }

    "Opportunity is published and ends before current date" should {
      "show as status Closed" in {
        val o: Opportunity = Opportunity (OpportunityId(1), "Test opportunity", testDateBeforeCurrent, Some(testDateBeforeCurrent), OpportunityValue(2000.00, "spondulix"), Some(publishedDate), None, Seq())
        o.statusString shouldBe "Closed"
      }
    }

    "Opportunity is not published" should {
      "show as status Draft" in {
        val o: Opportunity = Opportunity (OpportunityId(1), "Test opportunity", testDateBeforeCurrent, Some(testDateAfterCurrent), OpportunityValue(2000.00, "spondulix"), None, None, Seq())
        o.statusString shouldBe "Draft"
      }
    }

  }

}

