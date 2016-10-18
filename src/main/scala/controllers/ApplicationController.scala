package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import models.{ApplicationFormId, ApplicationSection}
import org.joda.time.LocalDateTime
import play.api.libs.json._
import play.api.mvc.{Action, Controller, Result}
import services.{ApplicationFormOps, ApplicationOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

class ApplicationController @Inject()(applications: ApplicationOps, applicationForms: ApplicationFormOps, opportunities: OpportunityOps)(implicit ec: ExecutionContext)
  extends Controller
    with ControllerUtils {

  def show(id: ApplicationFormId) = Action.async {
    val t = for {
      af <- OptionT(applicationForms.byId(id))
      a <- OptionT(applicationForms.overview(id))
    } yield (af, a)

    t.value.map {
      case Some((form, overview)) => Ok(views.html.showApplicationForm(form, overview))
      case None => NotFound
    }
  }

  def showSectionForm(id: ApplicationFormId, sectionNumber: Int) = Action.async {
    if (sectionNumber == 1) applications.getSection(id, sectionNumber).flatMap { section => title(id, section) }
    else Future.successful(Ok(views.html.wip(routes.ApplicationController.show(id).url)))
  }

  def title(id: ApplicationFormId, section: Option[ApplicationSection]) = {
    val ft = for {
      af <- OptionT(applicationForms.byId(id))
      o <- OptionT(opportunities.byId(af.opportunityId))
    } yield (af, o)

    ft.value.map {
      case Some((appForm, opp)) => Ok(views.html.titleForm(section, appForm, appForm.sections.find(_.sectionNumber == 1).get, opp))
      case None => NotFound
    }
  }

  /**
    * Note if more than one button action name is present in the keys then it is indeterminate as to
    * which one will be returned. This shouldn't occur if the form is properly submitted from a
    * browser, though.
    */
  def decodeAction(keys: Set[String]): Option[ButtonAction] = keys.flatMap(ButtonAction.unapply).headOption

  def postSection(id: ApplicationFormId, sectionNumber: Int) = Action.async(parse.urlFormEncoded) { implicit request =>
    takeAction(id, sectionNumber, decodeAction(request.body.keySet), formToJson(request.body.filterKeys(k => !k.startsWith("_"))))
  }

  def takeAction(id: ApplicationFormId, sectionNumber: Int, buttonAction: Option[ButtonAction], fieldValues: JsObject): Future[Result] = {
    val result: Option[Future[Unit]] = buttonAction.map {
      case Complete => applications.saveSection(id, sectionNumber, fieldValues, Some(LocalDateTime.now()))
      case Save => applications.saveSection(id, sectionNumber, fieldValues)
      case Preview => Future.successful(Unit)
    }

    result.map(f => f.map(_ => Redirect(routes.ApplicationController.show(id))))
      .getOrElse(Future.successful(BadRequest))
  }
}
