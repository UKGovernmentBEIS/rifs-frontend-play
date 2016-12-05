package services

import com.google.inject.ImplementedBy
import models._

import scala.concurrent.Future

@ImplementedBy(classOf[OpportunityService])
trait OpportunityOps {
  def byId(id: OpportunityId): Future[Option[Opportunity]]

  def getOpportunitySummaries: Future[Seq[Opportunity]]

  def getOpenOpportunitySummaries: Future[Seq[Opportunity]]

  def saveSummary(opp: OpportunitySummary): Future[Unit]

  def saveDescriptionSectionText(id: OpportunityId, sectionNo: Int, descSect: Option[String]): Future[Unit]

  def duplicate(id: OpportunityId): Future[Option[OpportunityId]]
}




