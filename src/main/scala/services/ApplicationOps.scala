package services

import com.google.inject.ImplementedBy
import models.{ApplicationFormId, ApplicationSection}
import play.api.libs.json.JsObject

import scala.concurrent.Future

@ImplementedBy(classOf[ApplicationService])
trait ApplicationOps {

  def saveSection(id: ApplicationFormId, sectionNumber: Int, doc: JsObject): Future[Unit]

  def getSection(id: ApplicationFormId, sectionNumber: Int): Future[Option[ApplicationSection]]
}
