package actions

import javax.inject.Inject

import models.{ApplicationId, ApplicationSectionDetail}
import play.api.mvc.Results._
import play.api.mvc._
import services.ApplicationOps

import scala.concurrent.{ExecutionContext, Future}

case class AppSectionRequest[A](appSection: ApplicationSectionDetail, request: Request[A]) extends WrappedRequest[A](request)

class AppSectionAction @Inject()(applications: ApplicationOps)(implicit ec: ExecutionContext) {
  def apply(id: ApplicationId, sectionNum: Int): ActionBuilder[AppSectionRequest] =
    new ActionBuilder[AppSectionRequest] {
      override def invokeBlock[A](request: Request[A], next: (AppSectionRequest[A]) => Future[Result]): Future[Result] = {
        applications.sectionDetail(id, sectionNum).flatMap {
          case Some(app) => next(AppSectionRequest(app, request))
          case None => Future.successful(NotFound(s"No application section with id ${id.id} and section number $sectionNum exists"))
        }
      }
    }
}