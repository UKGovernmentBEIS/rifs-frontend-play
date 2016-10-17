package services

import javax.inject.Inject

import com.wellfactored.playbindings.ValueClassFormats
import config.Config
import models.{ApplicationForm, ApplicationFormId, ApplicationFormSection, OpportunityId}
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class ApplicationService @Inject()(val ws: WSClient)(implicit val ec: ExecutionContext) extends ApplicationOps with RestService with ValueClassFormats {
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

  override def saveSection(id: ApplicationFormId, sectionNumber: Int, doc:JsObject): Future[Unit] = {
    val url = s"$baseUrl/application/${id.id}/section/$sectionNumber"
    post(url, doc)
  }

  override def getSection(id: ApplicationFormId, sectionNumber: Int): Future[Option[JsObject]] = {
    val url = s"$baseUrl/application/${id.id}/section/$sectionNumber"
    getOpt[JsObject](url)
  }
}