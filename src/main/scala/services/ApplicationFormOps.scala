package services

import com.google.inject.ImplementedBy
import models._

import scala.concurrent.Future

@ImplementedBy(classOf[ApplicationFormService])
trait ApplicationFormOps {
  def byId(id: ApplicationFormId): Future[Option[ApplicationForm]]

  def byOpportunityId(id: OpportunityId): Future[Option[ApplicationForm]]

  def overview(id:ApplicationFormId):Future[Option[ApplicationOverview]]
}


