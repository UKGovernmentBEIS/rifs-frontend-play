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

import javax.inject.Inject

import config.Config
import models._
import play.api.libs.ws.WSClient

class ApplicationFormURLs(baseUrl: String) {
  def applicationForm(id: ApplicationFormId) =
    s"$baseUrl/application_form/${id.id}"

  def application(applicationFormId: ApplicationFormId) =
    s"$baseUrl/application_form/${applicationFormId.id}/application"
}

import scala.concurrent.{ExecutionContext, Future}

class ApplicationFormService @Inject()(val ws: WSClient)(implicit val ec: ExecutionContext)
  extends ApplicationFormOps with RestService {

  val baseUrl = Config.config.business.baseUrl
  val urls = new ApplicationFormURLs(baseUrl)
  val oppUrls = new OpportunityURLs(baseUrl)

  override def byId(id: ApplicationFormId): Future[Option[ApplicationForm]] = {
    getOpt[ApplicationForm](urls.applicationForm(id))
  }

  override def byOpportunityId(id: OpportunityId): Future[Option[ApplicationForm]] = {
    getOpt[ApplicationForm](oppUrls.applicationForm(id))
  }
}