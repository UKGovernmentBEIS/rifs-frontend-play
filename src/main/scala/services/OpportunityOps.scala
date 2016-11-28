package services

import com.google.inject.ImplementedBy
import models._

import scala.concurrent.Future

@ImplementedBy(classOf[OpportunityService])
trait OpportunityOps {
  def byId(id: OpportunityId): Future[Option[Opportunity]]

  def getOpenOpportunitySummaries: Future[Seq[Opportunity]]

  def saveSummary(opp: OpportunitySummary): Future[Unit]
}




