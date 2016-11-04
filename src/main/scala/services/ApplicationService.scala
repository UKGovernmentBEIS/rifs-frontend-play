package services

import com.google.inject.Inject
import com.wellfactored.playbindings.ValueClassFormats
import config.Config
import controllers.FieldCheckHelpers
import controllers.FieldCheckHelpers.FieldErrors
import models._
import play.api.Logger
import play.api.libs.json.{JsDefined, JsNumber, JsObject, Json}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class ApplicationService @Inject()(val ws: WSClient)(implicit val ec: ExecutionContext)
  extends ApplicationOps with JodaFormats with RestService with ValueClassFormats {
  implicit val appSectionReads = Json.reads[ApplicationSection]
  implicit val appReads = Json.reads[Application]
  implicit val appSecOvRead = Json.reads[ApplicationSectionOverview]
  implicit val appOvRead = Json.reads[ApplicationOverview]

  val baseUrl = Config.config.business.baseUrl

  override def byId(id: ApplicationId): Future[Option[Application]] = {
    val url = s"$baseUrl/application/${id.id}"
    getOpt[Application](url)
  }

  override def saveSection(id: ApplicationId, sectionNumber: Int, doc: JsObject): Future[Unit] = {
    val url = s"$baseUrl/application/${id.id}/section/$sectionNumber"
    post(url, doc)
  }

  import controllers.ApplicationData._

  override def completeSection(id: ApplicationId, sectionNumber: Int, doc: JsObject): Future[FieldErrors] = {
    FieldCheckHelpers.check(doc, checksFor(sectionNumber)) match {
      case Nil =>
        val url = s"$baseUrl/application/${id.id}/section/$sectionNumber/complete"
        post(url, doc).map(_ => List())
      case errs => Future.successful(errs)
    }
  }

  override def saveItem(id: ApplicationId, sectionNumber: Int, doc: JsObject): Future[FieldErrors] = {
    Logger.debug(doc.toString)

    FieldCheckHelpers.check(doc, checksFor(sectionNumber)) match {
      case Nil =>
        val item = (doc \ "item").toOption.flatMap(_.validate[JsObject].asOpt).getOrElse(JsObject(Seq()))
        item \ "itemNumber" match {
          case JsDefined(JsNumber(itemNumber)) =>
            val url = s"$baseUrl/application/${id.id}/section/$sectionNumber/item/$itemNumber"
            put(url, item).map(_ => List())
          case _ =>
            val url = s"$baseUrl/application/${id.id}/section/$sectionNumber/items"
            post(url, item).map(_ => List())
        }
      case errs => Future.successful(errs)
    }
  }

  override def deleteItem(id: ApplicationId, sectionNumber: Int, itemNumber: Int): Future[Unit] = {
    val url = s"$baseUrl/application/${id.id}/section/$sectionNumber/item/$itemNumber"
    delete(url)
  }

  override def getSection(id: ApplicationId, sectionNumber: Int): Future[Option[ApplicationSection]] = {
    val url = s"$baseUrl/application/${id.id}/section/$sectionNumber"
    getOpt[ApplicationSection](url)
  }

  override def getOrCreateForForm(applicationFormId: ApplicationFormId): Future[Option[Application]] = {
    val url = s"$baseUrl/application_form/${applicationFormId.id}/application"
    getOpt[Application](url)
  }

  override def overview(id: ApplicationId): Future[Option[ApplicationOverview]] = {
    val url = s"$baseUrl/application/${id.id}/overview"
    getOpt[ApplicationOverview](url)
  }
}
