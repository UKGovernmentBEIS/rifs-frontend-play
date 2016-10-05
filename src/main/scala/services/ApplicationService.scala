package services

import javax.inject.Inject

import com.wellfactored.playbindings.ValueClassFormats
import config.Config
import models.{Application, ApplicationSection, OpportunityId}
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class ApplicationService @Inject()(ws: WSClient)(implicit ec: ExecutionContext) extends ApplicationOps with ValueClassFormats {
  implicit val appSecRead = Json.reads[ApplicationSection]
  implicit val appRead = Json.reads[Application]

  val baseUrl = Config.config.business.baseUrl

  override def byOpportunityId(id: OpportunityId): Future[Option[Application]] = {
    val url = s"$baseUrl/opportunity/${id.id}/application"
    ws.url(url).get.map { response =>
      response.status match {
        case 200 =>
          Logger.debug(s"body is ${response.body}")
          response.json.validate[Application].asOpt
        case s =>
          Logger.debug(s"got status $s calling $url")
          Logger.debug(s"body is ${response.body}")
          None
      }
    }
  }
}