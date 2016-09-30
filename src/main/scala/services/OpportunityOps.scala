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

class OpportunityService @Inject()(ws: WSClient)(implicit ec: ExecutionContext) extends OpportunityOps with ValueClassFormats {
  implicit val odsRead = Json.reads[OpportunityDescriptionSection]
  implicit val ovRead = Json.reads[OpportunityValue]
  implicit val odRead = Json.reads[OpportunityDuration]
  implicit val oppRead = Json.reads[Opportunity]

  override def getOpenOpportunities: Future[Seq[Opportunity]] = {
    val url = s"http://localhost:9100/opportunity/open"
    ws.url(url).get.map { response =>
      response.status match {
        case 200 => response.json.validate[Seq[Opportunity]].getOrElse(Seq())
        case s =>
          Logger.debug(s"got status $s calling $url")
          Seq()
      }
    }
  }

  override def getOpportunity(id: OpportunityId): Future[Option[Opportunity]] = {
    val url = s"http://localhost:9100/opportunity/${id.id}"
    ws.url(url).get.map { response =>
      response.status match {
        case 200 => response.json.validate[Opportunity].asOpt
        case s =>
          Logger.debug(s"got status $s calling $url")
          None
      }
    }
  }
}


