package services

import javax.inject.Inject

import com.wellfactored.playbindings.ValueClassFormats
import config.Config
import models._
import org.joda.time.DateTime
import play.api.libs.json.{Json, Reads, Writes}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class OpportunityService @Inject()(val ws: WSClient)(implicit val ec: ExecutionContext) extends OpportunityOps with RestService with ValueClassFormats {
  private val dtPattern = "dd MMM yyyy HH:mm:ss"
  implicit val dtReads = Reads.jodaDateReads(dtPattern)
  implicit val dtWrites = Writes.jodaDateWrites(dtPattern)

  implicit val jldReads = Reads.jodaLocalDateReads("d MMM yyyy")
  implicit val jldWrites = Writes.jodaLocalDateWrites("d MMM yyyy")
  implicit val odsFmt = Json.format[OpportunityDescriptionSection]
  implicit val ovFmt = Json.format[OpportunityValue]
  implicit val odFmt = Json.format[OpportunityDuration]
  implicit val oppFmt = Json.format[Opportunity]
  implicit val oppSummaryFmt = Json.format[OpportunitySummary]

  val baseUrl = Config.config.business.baseUrl

  override def getOpportunitySummaries: Future[Seq[Opportunity]] = {
    val url = s"$baseUrl/opportunity/summaries"
    getMany[Opportunity](url)
  }

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

  override def saveDescriptionSectionText(id: OpportunityId, sectionNo: Int, descSect: Option[String]): Future[Unit] = {
    val url = s"$baseUrl/manage/opportunity/${id.id}/description/$sectionNo"
    post(url, descSect.getOrElse(""))
  }

  override def duplicate(id: OpportunityId) :Future[Option[OpportunityId]] = {
    val url = s"$baseUrl/opportunity/${id.id}/duplicate"
    postWithResult[OpportunityId, String](url, "")
  }

  override def publish(id: OpportunityId) :Future[Option[DateTime]] = {
    val url = s"$baseUrl/opportunity/${id.id}/publish"
    postWithResult[DateTime, Option[String]](url, None)
  }
}