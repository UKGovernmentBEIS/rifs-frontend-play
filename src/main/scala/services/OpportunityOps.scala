package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import com.wellfactored.playbindings.ValueClassFormats
import models._
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[OpportunityService])
trait OpportunityOps {
  def getOpenOpportunities: Future[Seq[Opportunity]]

  def getOpportunity(id: OpportunityId): Future[Option[Opportunity]]
}




