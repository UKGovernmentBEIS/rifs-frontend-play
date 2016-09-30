package services

import models.{Opportunity, OpportunityDescriptionSection, OpportunityId, OpportunityValue}

import scala.concurrent.Future

class DummyOpportunities extends OpportunityOps {
  override def getOpenOpportunities: Future[Seq[Opportunity]] = Future.successful(opportunities)

  override def getOpportunity(id: OpportunityId): Future[Option[Opportunity]] = Future.successful {
    opportunities.find(_.id == id)
  }

  lazy val opportunities = Seq(opportunity1)

  lazy val opportunity1 = Opportunity(
    OpportunityId(1),
    "Research priorities in health care",
    "4 March 2017",
    None,
    OpportunityValue(2000, "per event maximum"),
    Seq(
      OpportunityDescriptionSection(1, "About this opportunity", Seq(
        "We want to achieve the widest benefit to society and the economy from the research we fund.",
        "As part of this, we want to help you to develop innovative ways of building on the research they carry out.",
        "This may be by sharing knowledge, commercialising ideas, exploring social benefits or other ways to increase the impact of your research.",
        "Under the Exploring Innovation Seminars programme, we will pay up to &pound;2,000 for each event promoting innovation and collaboration. We will not pay for food or drink.",
        "Only organisations which receive funding from UK Research Councils may apply."
      )),
      OpportunityDescriptionSection(2, "The events we will fund", Seq()),
      OpportunityDescriptionSection(3, "What events should cover", Seq()),
      OpportunityDescriptionSection(4, "How to get funding", Seq()),
      OpportunityDescriptionSection(5, "Assessment criteria", Seq()),
      OpportunityDescriptionSection(6, "Further information", Seq())

    )
  )


}
