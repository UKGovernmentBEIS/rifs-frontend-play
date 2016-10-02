package services

import com.google.inject.ImplementedBy
import models._

import scala.concurrent.Future

@ImplementedBy(classOf[OpportunityService])
trait OpportunityOps {
  def getOpenOpportunitySummaries: Future[Seq[Opportunity]]

  def getOpportunity(id: OpportunityId): Future[Option[Opportunity]]
}




