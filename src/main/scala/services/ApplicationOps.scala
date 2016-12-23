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

import com.google.inject.ImplementedBy
import controllers.FieldCheckHelpers.FieldErrors
import models._
import play.api.libs.json.{JsObject, Reads}

import scala.concurrent.Future

@ImplementedBy(classOf[ApplicationService])
trait ApplicationOps {

  def byId(id: ApplicationId): Future[Option[Application]]

  def getOrCreateForForm(applicationFormId: ApplicationFormId): Future[Option[Application]]

  def overview(id: ApplicationId): Future[Option[ApplicationOverview]]

  def detail(id: ApplicationId): Future[Option[ApplicationDetail]]

  def sectionDetail(id: ApplicationId, sectionNum:Int): Future[Option[ApplicationSectionDetail]]

  def reset(): Future[Unit]

  def saveSection(id: ApplicationId, sectionNumber: Int, doc: JsObject): Future[Unit]

  def completeSection(id: ApplicationId, sectionNumber: Int, doc: JsObject): Future[FieldErrors]

  def saveItem(id: ApplicationId, sectionNumber: Int, doc: JsObject): Future[FieldErrors]

  def getItem[T: Reads](id: ApplicationId, sectionNumber: Int, itemNumber: Int): Future[Option[T]]

  def deleteItem(id: ApplicationId, sectionNumber: Int, itemNumber: Int): Future[Unit]

  def getSection(id: ApplicationId, sectionNumber: Int): Future[Option[ApplicationSection]]

  def getSections(id: ApplicationId): Future[Seq[ApplicationSection]]

  def deleteSection(id: ApplicationId, sectionNumber: Int): Future[Unit]

  def clearSectionCompletedDate(id: ApplicationId, sectionNumber: Int): Future[Unit]

  def submit(id: ApplicationId): Future[Option[SubmittedApplicationRef]]

  def updatePersonalReference(id: ApplicationId, reference: String): Future[Unit]
}
