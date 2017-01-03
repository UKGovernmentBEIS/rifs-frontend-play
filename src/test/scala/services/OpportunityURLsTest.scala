package services

import models.{OppSectionNumber, OpportunityId}
import org.scalatest.{Matchers, WordSpecLike}
import eu.timepit.refined.auto._

class OpportunityURLsTest extends WordSpecLike with Matchers {
  val urls = new OpportunityURLs("")

  "OpporunityURLs" should {
    "generate correct url for summaries" in {
      urls.opportunitySummaries shouldBe "/opportunity/summaries"
    }

    "generate correct url for open summaries" in {
      urls.openOpportunitySummaries shouldBe "/opportunity/open/summaries"
    }

    val id = OpportunityId(1L)
    val sectionNumber = OppSectionNumber(2)

    "generate correct url for opportunity" in {
      urls.opportunity(id) shouldBe "/opportunity/1"
    }

    "generate correct url for opportunity summary" in {
      urls.summary(id) shouldBe "/opportunity/1/summary"
    }

    "generate correct url for description section text" in {
      urls.descriptionSectionText(id, sectionNumber) shouldBe "/manage/opportunity/1/description/2"
    }

    "generate correct url for duplicate" in {
      urls.duplicate(id) shouldBe "/opportunity/1/duplicate"
    }

    "generate correct url for publish" in {
      urls.publish(id) shouldBe "/opportunity/1/publish"
    }

    "generate correct url for applicationForm" in {
      urls.applicationForm(id) shouldBe "/opportunity/1/application_form"
    }
  }

}
