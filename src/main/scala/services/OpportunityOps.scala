package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import models.{Opportunity, OpportunityDescriptionSection, OpportunityId, OpportunityValue}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[DummyOpportunities])
trait OpportunityOps {
  def getOpenOpportunities: Future[Seq[Opportunity]]

  def getOpportunity(id: OpportunityId): Future[Option[Opportunity]]
}

class OpportunityService @Inject()(ws: WSClient)(implicit ec: ExecutionContext) extends OpportunityOps{
  override def getOpenOpportunities: Future[Seq[Opportunity]] = ???

  override def getOpportunity(id: OpportunityId): Future[Option[Opportunity]] = ???
}

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
      OpportunityDescriptionSection("About this opportunity", Seq(
        "We want to achieve the widest benefit to society and the economy from the research we fund.",
        "As part of this, we want to help you to develop innovative ways of building on the research they carry out.",
        "This may be by sharing knowledge, commercialising ideas, exploring social benefits or other ways to increase the impact of your research.",
        "Under the Exploring Innovation Seminars programme, we will pay up to &pound;2,000 for each event promoting innovation and collaboration. We will not pay for food or drink.",
        "Only organisations which receive funding from UK Research Councils may apply."
      )),
      OpportunityDescriptionSection("The events we will fund", Seq()),
      OpportunityDescriptionSection("What events should cover", Seq()),
      OpportunityDescriptionSection("How to get funding", Seq()),
      OpportunityDescriptionSection("Assessment criteria", Seq()),
      OpportunityDescriptionSection("Further information", Seq())

    )
  )


}
