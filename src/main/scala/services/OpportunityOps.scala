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
import models._
import org.joda.time.DateTime

import scala.concurrent.Future

@ImplementedBy(classOf[OpportunityService])
trait OpportunityOps {
  def byId(id: OpportunityId): Future[Option[Opportunity]]

  def getOpportunitySummaries: Future[Seq[Opportunity]]

  def getOpenOpportunitySummaries: Future[Seq[Opportunity]]

  def saveSummary(opp: OpportunitySummary): Future[Unit]

  def saveDescriptionSectionText(id: OpportunityId, sectionNum: OppSectionNumber, descSect: Option[String]): Future[Unit]

  def duplicate(id: OpportunityId): Future[Option[OpportunityId]]

  def publish(id: OpportunityId): Future[Option[DateTime]]
}




