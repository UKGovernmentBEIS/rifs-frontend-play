package services

import com.google.inject.Inject
import com.wellfactored.playbindings.ValueClassFormats
import config.Config
import models.{ApplicationFormId, ApplicationSection}
import org.joda.time.LocalDateTime
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class ApplicationService @Inject()(val ws: WSClient)(implicit val ec: ExecutionContext)
  extends ApplicationOps with JodaFormats with RestService with ValueClassFormats {
  implicit val appSectionReads = Json.reads[ApplicationSection]

  val baseUrl = Config.config.business.baseUrl

  override def saveSection(id: ApplicationFormId, sectionNumber: Int, doc: JsObject, completedAt: Option[LocalDateTime] = None): Future[Unit] = {
    val url = s"$baseUrl/application/${id.id}/section/$sectionNumber" + completedAt.map(_ => "/complete").getOrElse("")
    post(url, doc)
  }

  override def getSection(id: ApplicationFormId, sectionNumber: Int): Future[Option[ApplicationSection]] = {
    val url = s"$baseUrl/application/${id.id}/section/$sectionNumber"
    getOpt[ApplicationSection](url)
  }
}
