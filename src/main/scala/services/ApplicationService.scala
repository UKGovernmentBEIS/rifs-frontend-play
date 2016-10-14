package services

import javax.inject.Inject

import com.wellfactored.playbindings.ValueClassFormats
import config.Config
import models.{Application, ApplicationId, ApplicationSection, OpportunityId}
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class ApplicationService @Inject()(val ws: WSClient)(implicit val ec: ExecutionContext) extends ApplicationOps with RestService with ValueClassFormats {
  implicit val appSecRead = Json.reads[ApplicationSection]
  implicit val appRead = Json.reads[Application]

  val baseUrl = Config.config.business.baseUrl

  override def byId(id: ApplicationId): Future[Option[Application]] =  {
    val url = s"$baseUrl/application/${id.id}"
    getOpt[Application](url)
  }

  override def byOpportunityId(id: OpportunityId): Future[Option[Application]] = {
    val url = s"$baseUrl/opportunity/${id.id}/application"
    getOpt[Application](url)
  }


}