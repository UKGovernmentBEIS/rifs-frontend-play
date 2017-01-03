package services

import eu.timepit.refined.auto._
import models.{AppSectionNumber, ApplicationId}
import org.scalatest.{Matchers, WordSpecLike}

class ApplicationURLsTest extends WordSpecLike with Matchers {
  val urls = new ApplicationURLs("")

  "ApplicationURLsTest" should {

    val id = ApplicationId(1L)
    val sectionNum = AppSectionNumber(2)
    val itemNum = 3

    "generate correct url for items" in {
      urls.items(id, sectionNum) shouldBe "/application/1/section/2/items"
    }

    "generate correct url for personalRef" in {
      urls.personalRef(id) shouldBe "/application/1/personal-ref"
    }

    "generate correct url for section" in {
      urls.section(id, sectionNum) shouldBe "/application/1/section/2"
    }

    "generate correct url for sectionDetail" in {
      urls.sectionDetail(id, sectionNum) shouldBe "/application/1/section/2/detail"
    }

    "generate correct url for detail" in {
      urls.detail(id) shouldBe "/application/1/detail"
    }

    "generate correct url for application" in {
      urls.application(id) shouldBe "/application/1"
    }

    "generate correct url for sections" in {
      urls.sections(id) shouldBe "/application/1/sections"
    }

    "generate correct url for complete" in {
      urls.complete(id, sectionNum) shouldBe "/application/1/section/2/complete"
    }

    "generate correct url for item" in {
      urls.item(id, sectionNum, itemNum) shouldBe "/application/1/section/2/item/3"
    }

    "generate correct url for markNotCompleted" in {
      urls.markNotCompleted(id, sectionNum) shouldBe "/application/1/section/2/markNotCompleted"
    }

    "generate correct url for reset" in {
      urls.reset shouldBe "/reset"
    }

    "generate correct url for submit" in {
      urls.submit(id) shouldBe "/application/1/submit"
    }

  }
}
