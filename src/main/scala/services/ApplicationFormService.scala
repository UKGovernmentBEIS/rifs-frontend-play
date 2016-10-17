package services

import javax.inject.Inject

import com.wellfactored.playbindings.ValueClassFormats
import config.Config
import models.{ApplicationForm, ApplicationFormId, ApplicationFormSection, OpportunityId}
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class ApplicationFormService @Inject()(val ws: WSClient)(implicit val ec: ExecutionContext) extends ApplicationFormOps with RestService with ValueClassFormats {
  implicit val appSecRead = Json.reads[ApplicationFormSection]
  implicit val appRead = Json.reads[ApplicationForm]

  val baseUrl = Config.config.business.baseUrl

  override def byId(id: ApplicationFormId): Future[Option[ApplicationForm]] =  {
    val url = s"$baseUrl/application_form/${id.id}"
    getOpt[ApplicationForm](url)
  }

  override def byOpportunityId(id: OpportunityId): Future[Option[ApplicationForm]] = {
    val url = s"$baseUrl/opportunity/${id.id}/application_form"
    getOpt[ApplicationForm](url)
  }


}