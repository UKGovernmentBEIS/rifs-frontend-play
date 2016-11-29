package services

import javax.inject.Inject

import com.wellfactored.playbindings.ValueClassFormats
import config.Config
import models._
import play.api.libs.json.{Json, Reads, Writes}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class OpportunityService @Inject()(val ws: WSClient)(implicit val ec: ExecutionContext) extends OpportunityOps with RestService with ValueClassFormats {
  implicit val jldReads = Reads.jodaLocalDateReads("d MMM yyyy")
  implicit val jldWrites = Writes.jodaLocalDateWrites("d MMM yyyy")
  implicit val odsFmt = Json.format[OpportunityDescriptionSection]
  implicit val ovFmt = Json.format[OpportunityValue]
  implicit val odFmt = Json.format[OpportunityDuration]
  implicit val oppFmt = Json.format[Opportunity]
  implicit val oppSummaryFmt = Json.format[OpportunitySummary]

  val baseUrl = Config.config.business.baseUrl

  override def getOpenOpportunitySummaries: Future[Seq[Opportunity]] = {
    val url = s"$baseUrl/opportunity/open/summaries"
    getMany[Opportunity](url)
  }

  override def byId(id: OpportunityId): Future[Option[Opportunity]] = {
    val url = s"$baseUrl/opportunity/${id.id}"
    getOpt[Opportunity](url)
  }

  override def saveSummary(opp: OpportunitySummary): Future[Unit] = {
    val url = s"$baseUrl/opportunity/${opp.id.id}/summary"
    put(url, opp)
  }
}