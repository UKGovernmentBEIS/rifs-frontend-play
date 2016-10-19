package services

import com.google.inject.ImplementedBy
import models._
import org.joda.time.LocalDateTime
import play.api.libs.json.JsObject

import scala.concurrent.Future

@ImplementedBy(classOf[ApplicationService])
trait ApplicationOps {

  def byId(id: ApplicationId): Future[Option[Application]]

  def getOrCreateForForm(applicationFormId: ApplicationFormId): Future[Option[Application]]

  def overview(id: ApplicationId): Future[Option[ApplicationOverview]]

  def saveSection(id: ApplicationId, sectionNumber: Int, doc: JsObject, completedAt: Option[LocalDateTime] = None): Future[Unit]

  def getSection(id: ApplicationId, sectionNumber: Int): Future[Option[ApplicationSection]]
}
