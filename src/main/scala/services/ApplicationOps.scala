package services

import com.google.inject.ImplementedBy
import models.{Application, ApplicationId, OpportunityId}
import play.api.libs.json.JsObject

import scala.concurrent.Future

@ImplementedBy(classOf[ApplicationService])
trait ApplicationOps {
  def byId(id: ApplicationId): Future[Option[Application]]

  def byOpportunityId(id: OpportunityId): Future[Option[Application]]

  def saveSection(id: ApplicationId, sectionNumber: Int, doc: JsObject): Future[Unit]

  def getSection(id: ApplicationId, sectionNumber: Int): Future[Option[JsObject]]
}
