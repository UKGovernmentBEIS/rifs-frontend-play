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

package controllers

import javax.inject.Inject

import eu.timepit.refined.auto._
import actions.{OppSectionAction, OpportunityAction}
import models.{AppSectionNumber, OppSectionNumber, OpportunityId}
import play.api.mvc.{Action, Controller}
import services.{ApplicationFormOps, OpportunityOps}

import scala.concurrent.ExecutionContext

class OpportunityController @Inject()(
                                       opportunities: OpportunityOps,
                                       appForms: ApplicationFormOps,
                                       OpportunityAction: OpportunityAction,
                                       OppSectionAction: OppSectionAction
                                     )(implicit ec: ExecutionContext) extends Controller {

  def showOpportunities = Action.async {
    opportunities.getOpenOpportunitySummaries.map { os => Ok(views.html.showOpportunities(os)) }
  }

  def showOpportunity(id: OpportunityId, sectionNumber: Option[OppSectionNumber]) = OpportunityAction(id) { request =>
    Redirect(controllers.routes.OpportunityController.showOpportunitySection(id, sectionNumber.getOrElse(OppSectionNumber(1))))
  }

  def showOpportunitySection(id: OpportunityId, sectionNum: OppSectionNumber) = OppSectionAction(id, sectionNum).async { request =>
    appForms.byOpportunityId(id).map {
      case Some(appForm) => Ok(views.html.showOpportunity(appForm, request.opportunity, request.section))
      case None => NotFound
    }
  }

  def showGuidancePage(id: OpportunityId) = Action {
    Ok(views.html.guidance(id))
  }

  def wip(backUrl: String) = Action {
    Ok(views.html.wip(backUrl))
  }

}



