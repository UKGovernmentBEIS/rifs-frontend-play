package actions

import javax.inject.Inject

import models.{Opportunity, OpportunityDescriptionSection, OpportunityId}
import play.api.mvc.Results.NotFound
import play.api.mvc.{ActionBuilder, Request, Result, WrappedRequest}
import services.OpportunityOps

import scala.concurrent.{ExecutionContext, Future}

case class OppSectionRequest[A](opportunity: Opportunity, section: OpportunityDescriptionSection, request: Request[A]) extends WrappedRequest[A](request)

class OppSectionAction @Inject()(opportunities: OpportunityOps)(implicit ec: ExecutionContext) {
  def apply(id: OpportunityId, sectionNum: Int): ActionBuilder[OppSectionRequest] =
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
