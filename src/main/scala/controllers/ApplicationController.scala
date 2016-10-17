package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import models.ApplicationFormId
import play.api.Logger
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}
import play.api.mvc.{Action, Controller, Result}
import services.{ApplicationFormOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

class ApplicationController @Inject()(applicationForms: ApplicationFormOps, opportunities: OpportunityOps)(implicit ec: ExecutionContext) extends Controller {

  def show(id: ApplicationFormId) = Action.async {
    applicationForms.byId(id).map {
      case Some(application) => Ok(views.html.showApplicationForm(application))
      case None => NotFound
    }
  }

  def sectionForm(id: ApplicationFormId, sectionNumber: Int) = Action.async {
    if (sectionNumber == 1) applicationForms.getSection(id, sectionNumber).flatMap { doco => title(id, doco) }
    else Future.successful(Ok(views.html.wip(routes.ApplicationController.show(id).url)))
  }

  def title(id: ApplicationFormId, formValues: Option[JsObject] = None) = {
    val ft = for {
      a <- OptionT(applicationForms.byId(id))
      o <- OptionT(opportunities.byId(a.opportunityId))
    } yield (a, o)


    ft.value.map {
      case Some((app, opp)) => Ok(views.html.titleForm(formValues.getOrElse(JsObject(Seq())), app, app.sections.find(_.sectionNumber == 1).get, opp))
      case None => NotFound
    }
  }

  /**
    * Note if more than one button action name is present in the keys then it is indeterminate as to
    * which one will be returned.
    */
  def decodeAction(keys: Set[String]): Option[ButtonAction] = keys.flatMap(ButtonAction.unapply).headOption

  def section(id: ApplicationFormId, sectionNumber: Int) = Action.async(parse.urlFormEncoded) { implicit request =>
    Logger.debug(request.body.toString)

    val buttonAction: Option[ButtonAction] = decodeAction(request.body.keySet)
    Logger.debug(s"Button action is $buttonAction")

    val jmap: Map[String, JsValue] = request.body.map {
      case (k, s :: Nil) => k -> JsString(s)
      case (k, ss) => k -> JsArray(ss.map(JsString))
    }

    takeAction(id, sectionNumber, buttonAction, JsObject(jmap))
  }

  def takeAction(id: ApplicationFormId, sectionNumber: Int, buttonAction: Option[ButtonAction], doc: JsObject): Future[Result] = {
    buttonAction.map {
      case Complete => Future.successful(Redirect(routes.ApplicationController.show(id)))
      case Save =>
        applicationForms.saveSection(id, sectionNumber, doc).map { _ =>
          Redirect(routes.ApplicationController.show(id))
        }
      case Preview => Future.successful(Redirect(routes.ApplicationController.show(id)))
    }.getOrElse(Future.successful(BadRequest))
  }
}
