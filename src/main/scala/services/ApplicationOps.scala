package services

import com.google.inject.ImplementedBy
import controllers.FieldCheckHelpers.FieldErrors
import models._
import play.api.libs.json.JsObject

import scala.concurrent.Future

@ImplementedBy(classOf[ApplicationService])
trait ApplicationOps {

  def byId(id: ApplicationId): Future[Option[Application]]

  def getOrCreateForForm(applicationFormId: ApplicationFormId): Future[Option[Application]]

  def overview(id: ApplicationId): Future[Option[ApplicationOverview]]

  def saveSection(id: ApplicationId, sectionNumber: Int, doc: JsObject): Future[Unit]

  def completeSection(id: ApplicationId, sectionNumber: Int, doc: JsObject): Future[FieldErrors]

  def saveItem(id: ApplicationId, sectionNumber: Int, doc: JsObject): Future[FieldErrors]

  def deleteItem(id: ApplicationId, sectionNumber: Int, itemNumber: Int): Future[Unit]

  def getSection(id: ApplicationId, sectionNumber: Int): Future[Option[ApplicationSection]]

  def deleteSection(id: ApplicationId, sectionNumber: Int): Future[Unit]

}
