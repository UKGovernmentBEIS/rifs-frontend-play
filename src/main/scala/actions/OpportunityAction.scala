package actions

import javax.inject.Inject

import models.{Opportunity, OpportunityId}
import play.api.mvc.Results._
import play.api.mvc._
import services.OpportunityOps

import scala.concurrent.{ExecutionContext, Future}

case class OpportunityRequest[A](opportunity: Opportunity, request: Request[A]) extends WrappedRequest[A](request)

class OpportunityAction @Inject()(opportunities: OpportunityOps)(implicit ec:ExecutionContext) {
  def apply(id: OpportunityId): ActionBuilder[OpportunityRequest] =
    new ActionBuilder[OpportunityRequest] {
      override def invokeBlock[A](request: Request[A], next: (OpportunityRequest[A]) => Future[Result]): Future[Result] = {
        opportunities.byId(id).flatMap {
          case Some(opp) => next(OpportunityRequest(opp, request))
          case None => Future.successful(NotFound(s"No opportunity with id ${id.id} exists"))
        }
      }
    }
}

