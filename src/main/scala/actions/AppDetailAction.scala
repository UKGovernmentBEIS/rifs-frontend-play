package actions

import javax.inject.Inject

import models.{Application, ApplicationDetail, ApplicationId}
import play.api.mvc.Results._
import play.api.mvc._
import services.ApplicationOps

import scala.concurrent.{ExecutionContext, Future}

case class AppDetailRequest[A](appDetail: ApplicationDetail, request: Request[A]) extends WrappedRequest[A](request)

class AppDetailAction @Inject()(applications: ApplicationOps)(implicit ec: ExecutionContext) {
  def apply(id: ApplicationId): ActionBuilder[AppDetailRequest] =
    new ActionBuilder[AppDetailRequest] {
      override def invokeBlock[A](request: Request[A], next: (AppDetailRequest[A]) => Future[Result]): Future[Result] = {
        applications.detail(id).flatMap {
          case Some(app) => next(AppDetailRequest(app, request))
          case None => Future.successful(NotFound(s"No application with id ${id.id} exists"))
        }
      }
    }
}