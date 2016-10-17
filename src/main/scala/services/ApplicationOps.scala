package services

import com.google.inject.ImplementedBy
import models.{ApplicationForm, ApplicationFormId, OpportunityId}
import play.api.libs.json.JsObject

import scala.concurrent.Future

@ImplementedBy(classOf[ApplicationService])
trait ApplicationOps {
  def byId(id: ApplicationFormId): Future[Option[ApplicationForm]]

  def byOpportunityId(id: OpportunityId): Future[Option[ApplicationForm]]

  def saveSection(id: ApplicationFormId, sectionNumber: Int, doc: JsObject): Future[Unit]

  def getSection(id: ApplicationFormId, sectionNumber: Int): Future[Option[JsObject]]
}
