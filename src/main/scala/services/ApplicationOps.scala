package services

import com.google.inject.ImplementedBy
import models.{Application, OpportunityId}

import scala.concurrent.Future

@ImplementedBy(classOf[ApplicationService])
trait ApplicationOps {
  def byOpportunityId(id:OpportunityId):Future[Option[Application]]
}
