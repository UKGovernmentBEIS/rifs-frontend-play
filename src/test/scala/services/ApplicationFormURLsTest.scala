package services

import eu.timepit.refined.auto._
import models.ApplicationFormId
import org.scalatest.{Matchers, WordSpecLike}

class ApplicationFormURLsTest extends WordSpecLike with Matchers {

  "ApplicationFormURLsTest" should {
    val urls = new ApplicationFormURLs("")
    val id = ApplicationFormId(1L)

    "generate correct url for applicationForm" in {
      urls.applicationForm(id) shouldBe "/application_form/1"
    }

    "generate correct url for application" in {
      urls.application(id) shouldBe "/application_form/1/application"
    }
  }
}
