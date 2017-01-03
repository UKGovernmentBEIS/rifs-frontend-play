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

package actions

import javax.inject.Inject

import models.{OppSectionNumber, Opportunity, OpportunityDescriptionSection, OpportunityId}
import play.api.mvc.Results.NotFound
import play.api.mvc.{ActionBuilder, Request, Result, WrappedRequest}
import services.OpportunityOps

import scala.concurrent.{ExecutionContext, Future}

case class OppSectionRequest[A](opportunity: Opportunity, section: OpportunityDescriptionSection, request: Request[A]) extends WrappedRequest[A](request)

class OppSectionAction @Inject()(opportunities: OpportunityOps)(implicit ec: ExecutionContext) {
  def apply(id: OpportunityId, sectionNum: OppSectionNumber): ActionBuilder[OppSectionRequest] =
    new ActionBuilder[OppSectionRequest] {
      override def invokeBlock[A](request: Request[A], next: (OppSectionRequest[A]) => Future[Result]): Future[Result] = {
        opportunities.byId(id).flatMap {
          case Some(opp) =>
            opp.description.find(_.sectionNumber == sectionNum) match {
              case Some(section) => next(OppSectionRequest(opp, section, request))
              case None => Future.successful(NotFound(s"No section with number $sectionNum exists on opportunity with id ${id.id}"))
            }
          case None => Future.successful(NotFound(s"No opportunity with id ${id.id} exists"))
        }
      }
    }
}
