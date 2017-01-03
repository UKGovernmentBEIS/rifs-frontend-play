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
import org.joda.time.DateTime
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class OpportunityURLs(baseUrl: String) {
  val opportunitySummaries: String = s"$baseUrl/opportunity/summaries"
  val openOpportunitySummaries: String = s"$baseUrl/opportunity/open/summaries"

  def opportunity(id: OpportunityId): String = s"$baseUrl/opportunity/${id.id}"

  def summary(id: OpportunityId): String = s"$baseUrl/opportunity/${id.id}/summary"

  def descriptionSectionText(id: OpportunityId, sectionNum: OppSectionNumber): String = s"$baseUrl/manage/opportunity/${id.id}/description/${sectionNum.num}"

  def duplicate(id: OpportunityId): String = s"$baseUrl/opportunity/${id.id}/duplicate"

  def publish(id: OpportunityId): String = s"$baseUrl/opportunity/${id.id}/publish"

  def applicationForm(id:OpportunityId) = s"$baseUrl/opportunity/${id.id}/application_form"
}

class OpportunityService @Inject()(val ws: WSClient)(implicit val ec: ExecutionContext)
  extends OpportunityOps with RestService {

  val baseUrl = Config.config.business.baseUrl
  val urls = new OpportunityURLs(baseUrl)

  override def getOpportunitySummaries: Future[Seq[Opportunity]] =
    getMany[Opportunity](urls.opportunitySummaries)

  override def getOpenOpportunitySummaries: Future[Seq[Opportunity]] =
    getMany[Opportunity](urls.openOpportunitySummaries)

  override def byId(id: OpportunityId): Future[Option[Opportunity]] =
    getOpt[Opportunity](urls.opportunity(id))

  override def saveSummary(opp: OpportunitySummary): Future[Unit] =
    put(urls.summary(opp.id), opp)

  override def saveDescriptionSectionText(id: OpportunityId, sectionNum: OppSectionNumber, descSect: Option[String]): Future[Unit] =
    post(urls.descriptionSectionText(id, sectionNum), descSect.getOrElse(""))

  override def duplicate(id: OpportunityId): Future[Option[OpportunityId]] = postWithResult[OpportunityId, String](urls.duplicate(id), "")

  override def publish(id: OpportunityId): Future[Option[DateTime]] = postWithResult[DateTime, Option[String]](urls.publish(id), None)
}