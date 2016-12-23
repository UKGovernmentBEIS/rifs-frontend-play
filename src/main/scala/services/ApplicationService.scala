/*
 * Copyright (C) 2016  Department for Business, Energy and Industrial Strategy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package services

import com.google.inject.Inject
import com.wellfactored.playbindings.ValueClassFormats
import config.Config
import controllers.FieldCheckHelpers.FieldErrors
import controllers.{FieldCheck, FieldCheckHelpers, FieldChecks}
import forms.validation.{CostItem, CostItemValues, CostSectionValidator, FieldError}
import models._
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class ApplicationService @Inject()(val ws: WSClient)(implicit val ec: ExecutionContext)
  extends ApplicationOps with JodaFormats with RestService with ValueClassFormats {
  private val dtPattern = "dd MMM yyyy HH:mm:ss"
  implicit val dtReads = Reads.jodaDateReads(dtPattern)
  implicit val dtWrites = Writes.jodaDateWrites(dtPattern)

  implicit val jldReads = Reads.jodaLocalDateReads("d MMM yyyy")
  implicit val fieldReads = fields.FieldReads.fieldReads
  implicit val appSectionReads = Json.reads[ApplicationSection]
  implicit val appReads = Json.reads[Application]
  implicit val appSecOvRead = Json.reads[ApplicationSectionOverview]
  implicit val appOvRead = Json.reads[ApplicationOverview]
  implicit val saRefReads = Json.reads[SubmittedApplicationRef]
  implicit val oppSecReads = Json.reads[OpportunityDescriptionSection]
  implicit val oppValueReads = Json.reads[OpportunityValue]
  implicit val oppDurReads = Json.reads[OpportunityDuration]
  implicit val oppSummaryReads = Json.reads[OpportunitySummary]
  implicit val oppReads = Json.reads[Opportunity]
  implicit val appFormQReads = Json.reads[ApplicationFormQuestion]
  implicit val appFormSecReads = Json.reads[ApplicationFormSection]
  implicit val appFormReads = Json.reads[ApplicationForm]
  implicit val appDetailReads = Json.reads[ApplicationDetail]
  implicit val appSecDetailReads = Json.reads[ApplicationSectionDetail]

  val baseUrl = Config.config.business.baseUrl

  override def byId(id: ApplicationId): Future[Option[Application]] = {
    val url = s"$baseUrl/application/${id.id}"
    getOpt[Application](url)
  }

  override def saveSection(id: ApplicationId, sectionNumber: Int, doc: JsObject): Future[Unit] = {
    val url = s"$baseUrl/application/${id.id}/section/$sectionNumber"
    post(url, doc)
  }

  override def completeSection(id: ApplicationId, sectionNumber: Int, doc: JsObject): Future[FieldErrors] = {
    sectionDetail(id, sectionNumber).flatMap {
      case Some(app) =>
        FieldCheckHelpers.check(doc, checksFor(app.formSection)) match {
          case Nil =>
            val url = s"$baseUrl/application/${id.id}/section/$sectionNumber/complete"
            post(url, doc).map(_ => List())
          case errs => Future.successful(errs)
        }
      // TODO: Need better error handling here
      case None => Future.successful(List(FieldError("", s"tried to save a non-existent section number $sectionNumber in application ${id.id}")))
    }
  }

  implicit val civReads = Json.reads[CostItemValues]
  implicit val ciReads = Json.reads[CostItem]

  def checksFor(formSection: ApplicationFormSection): Map[String, FieldCheck] =
    formSection.sectionType match {
      case SectionTypeForm => formSection.fields.map(f => f.name -> f.check).toMap
      case SectionTypeList => Map("items" -> FieldChecks.fromValidator(CostSectionValidator(2000)))
    }


  override def saveItem(id: ApplicationId, sectionNumber: Int, doc: JsObject): Future[FieldErrors] = {
    val item = (doc \ "item").toOption.flatMap(_.validate[JsObject].asOpt).getOrElse(JsObject(Seq()))
    item \ "itemNumber" match {
      case JsDefined(JsNumber(itemNumber)) =>
        val url = s"$baseUrl/application/${id.id}/section/$sectionNumber/item/$itemNumber"
        put(url, item).map(_ => List())
      case _ =>
        val url = s"$baseUrl/application/${id.id}/section/$sectionNumber/items"
        post(url, item).map(_ => List())
    }
  }

  override def deleteItem(id: ApplicationId, sectionNumber: Int, itemNumber: Int): Future[Unit] = {
    val url = s"$baseUrl/application/${id.id}/section/$sectionNumber/item/$itemNumber"
    delete(url)
  }

  override def getItem[T: Reads](id: ApplicationId, sectionNumber: Int, itemNumber: Int): Future[Option[T]] = {
    val url = s"$baseUrl/application/${id.id}/section/$sectionNumber/item/$itemNumber"
    getOpt[T](url)
  }

  override def getSection(id: ApplicationId, sectionNumber: Int): Future[Option[ApplicationSection]] = {
    val url = s"$baseUrl/application/${id.id}/section/$sectionNumber"
    getOpt[ApplicationSection](url)
  }

  override def getSections(id: ApplicationId): Future[Seq[ApplicationSection]] = {
    val url = s"$baseUrl/application/${id.id}/sections"
    getMany[ApplicationSection](url)
  }

  override def getOrCreateForForm(applicationFormId: ApplicationFormId): Future[Option[Application]] = {
    val url = s"$baseUrl/application_form/${applicationFormId.id}/application"
    getOpt[Application](url)
  }

  override def overview(id: ApplicationId): Future[Option[ApplicationOverview]] = {
    val url = s"$baseUrl/application/${id.id}"
    getOpt[ApplicationOverview](url)
  }

  override def detail(id: ApplicationId): Future[Option[ApplicationDetail]] = {
    val url = s"$baseUrl/application/${id.id}/detail"
    getOpt[ApplicationDetail](url)
  }

  override def sectionDetail(id: ApplicationId, sectionNum: Int): Future[Option[ApplicationSectionDetail]] = {
    val url = s"$baseUrl/application/${id.id}/section/$sectionNum/detail"
    getOpt[ApplicationSectionDetail](url)
  }

  override def reset(): Future[Unit] = {
    val url = s"$baseUrl/reset"
    post(url, None)
  }

  override def deleteSection(id: ApplicationId, sectionNumber: Int): Future[Unit] = {
    val url = s"$baseUrl/application/${id.id}/section/$sectionNumber"
    delete(url)
  }

  override def clearSectionCompletedDate(id: ApplicationId, sectionNumber: Int): Future[Unit] = {
    val url = s"$baseUrl/application/${id.id}/section/$sectionNumber/markNotCompleted"
    put(url, None)
  }

  override def submit(id: ApplicationId): Future[Option[SubmittedApplicationRef]] = {
    val url = s"$baseUrl/application/${id.id}/submit"
    postWithResult[SubmittedApplicationRef, String](url, "")
  }

  override def updatePersonalReference(id: ApplicationId, reference: String) = {
    val url = s"$baseUrl/application/${id.id}/personal-ref"
    post(url, reference)
  }
}
