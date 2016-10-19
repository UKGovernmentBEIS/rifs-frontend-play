package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import forms.{MandatoryRule, WordCountRule}
import models.{ApplicationFormId, ApplicationId, ApplicationSection}
import org.joda.time.LocalDateTime
import play.api.libs.json._
import play.api.mvc.{Action, Controller, Result}
import services.{ApplicationFormOps, ApplicationOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

class ApplicationController @Inject()(applications: ApplicationOps, applicationForms: ApplicationFormOps, opportunities: OpportunityOps)(implicit ec: ExecutionContext)
  extends Controller
    with ControllerUtils {

  def showOrCreateForForm(id: ApplicationFormId) = Action.async {
    applications.getOrCreateForForm(id).map {
      case Some(app) => Redirect(controllers.routes.ApplicationController.show(app.id))
      case None => NotFound
    }
  }

  def show(id: ApplicationId) = Action.async {
    val t = for {
      a <- OptionT(applications.overview(id))
      af <- OptionT(applicationForms.byId(a.applicationFormId))
    } yield (af, a)

    t.value.map {
      case Some((form, overview)) => Ok(views.html.showApplicationForm(form, overview))
      case None => NotFound
    }
  }

  def showSectionForm(id: ApplicationId, sectionNumber: Int) = Action.async {
    if (sectionNumber == 1) applications.getSection(id, sectionNumber).flatMap { section => title(id, section) }
    else Future.successful(Ok(views.html.wip(routes.ApplicationController.show(id).url)))
  }

  def title(id: ApplicationId, section: Option[ApplicationSection]) = {
    val rules = Map("title" -> Seq(WordCountRule(20), MandatoryRule))

    val ft = for {
      a<- OptionT(applications.byId(id))
      af <- OptionT(applicationForms.byId(a.applicationFormId))
      o <- OptionT(opportunities.byId(af.opportunityId))
    } yield (a, af, o)

    ft.value.map {
      case Some((app, appForm, opp)) => Ok(views.html.titleForm(app, section, appForm, appForm.sections.find(_.sectionNumber == 1).get, opp, rules))
      case None => NotFound
    }
  }

  /**
    * Note if more than one button action name is present in the keys then it is indeterminate as to
    * which one will be returned. This shouldn't occur if the form is properly submitted from a
    * browser, though.
    */
  def decodeButton(keys: Set[String]): Option[ButtonAction] = keys.flatMap(ButtonAction.unapply).headOption

  def postSection(id: ApplicationId, sectionNumber: Int) = Action.async(parse.urlFormEncoded) { implicit request =>
    // Drop keys that start with '_' as these are "system" keys like the button name
    val jsonFormValues = formToJson(request.body.filterKeys(k => !k.startsWith("_")))
    val button: Option[ButtonAction] = decodeButton(request.body.keySet)

    takeAction(id, sectionNumber, button, jsonFormValues)
  }

  def takeAction(id: ApplicationId, sectionNumber: Int, button: Option[ButtonAction], fieldValues: JsObject): Future[Result] = {
    val result: Option[Future[Unit]] = button.map {
      case Complete => applications.saveSection(id, sectionNumber, fieldValues, Some(LocalDateTime.now()))
      case Save => applications.saveSection(id, sectionNumber, fieldValues)
      case Preview => Future.successful(Unit)
    }

    result.map(f => f.map(_ => Redirect(routes.ApplicationController.show(id))))
      .getOrElse(Future.successful(BadRequest))
  }
}
