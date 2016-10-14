package services

import com.google.inject.ImplementedBy
import models.{Application, ApplicationId, OpportunityId}

import scala.concurrent.Future

@ImplementedBy(classOf[ApplicationService])
trait ApplicationOps {
  def byId(id: ApplicationId): Future[Option[Application]]

  def byOpportunityId(id: OpportunityId): Future[Option[Application]]
}
