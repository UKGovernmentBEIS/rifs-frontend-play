package services

import javax.inject.Inject

import com.wellfactored.playbindings.ValueClassFormats
import config.Config
import models._
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class ApplicationFormService @Inject()(val ws: WSClient)(implicit val ec: ExecutionContext)
  extends ApplicationFormOps with JodaFormats with RestService with ValueClassFormats {

  implicit val fieldReads = fields.FieldReads.fieldReads
  implicit val appFormQuestionReads = Json.reads[ApplicationFormQuestion]
  implicit val appSecRead = Json.reads[ApplicationFormSection]
  implicit val appRead = Json.reads[ApplicationForm]

  val baseUrl = Config.config.business.baseUrl

  override def byId(id: ApplicationFormId): Future[Option[ApplicationForm]] = {
    val url = s"$baseUrl/application_form/${id.id}"
    getOpt[ApplicationForm](url)
  }

  override def byOpportunityId(id: OpportunityId): Future[Option[ApplicationForm]] = {
    val url = s"$baseUrl/opportunity/${id.id}/application_form"
    getOpt[ApplicationForm](url)
  }
}