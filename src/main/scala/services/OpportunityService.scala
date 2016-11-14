package services

import javax.inject.Inject

import com.wellfactored.playbindings.ValueClassFormats
import config.Config
import models._
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class OpportunityService @Inject()(val ws: WSClient)(implicit val ec: ExecutionContext) extends OpportunityOps with RestService with ValueClassFormats {
  implicit val odsRead = Json.reads[OpportunitySection]
  implicit val ovRead = Json.reads[OpportunityValue]
  implicit val odRead = Json.reads[OpportunityDuration]
  implicit val oppRead = Json.reads[Opportunity]

  val baseUrl = Config.config.business.baseUrl

  override def getOpenOpportunitySummaries: Future[Seq[Opportunity]] = {
    val url = s"$baseUrl/opportunity/open/summaries"
    getMany[Opportunity](url)
  }

  override def byId(id: OpportunityId): Future[Option[Opportunity]] = {
    val url = s"$baseUrl/opportunity/${id.id}"
    getOpt[Opportunity](url)
  }
}