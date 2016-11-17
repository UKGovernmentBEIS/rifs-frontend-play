package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import forms.Field
import models._
import play.api.libs.json.JsObject
import play.api.mvc.{Action, Controller}
import play.twirl.api.Html
import services.{ApplicationFormOps, ApplicationOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

class ApplicationPreviewController @Inject()(actionHandler: ActionHandler, applications: ApplicationOps, appForms: ApplicationFormOps, opps: OpportunityOps)(implicit ec: ExecutionContext)
  extends Controller {

  def previewSection(id: ApplicationId, sectionNumber: Int) = Action.async { request =>
    val ft = actionHandler.gatherSectionDetails(id, sectionNumber)

    ft.flatMap {
      case Some((app, appForm, appFormSection, opp)) =>
        applications.getSection(id, sectionNumber).flatMap { section =>
          section.map(_.isComplete) match {
            case Some(true) => renderSectionPreviewCompleted(id, sectionNumber, section, appFormSection.fields)
            case _ => renderSectionPreviewInProgress(id, sectionNumber, section, appFormSection.fields)
          }
        }

      case None => Future(NotFound)
    }
  }

  def renderSectionPreviewCompleted(id: ApplicationId, sectionNumber: Int, section: Option[ApplicationSection], fields: Seq[Field]) = {
    val ft = gatherApplicationDetails(id)
    val answers = section.map { s => s.answers }.getOrElse(JsObject(Seq()))

    ft.map {
      case Some((app, appForm, opp)) =>
        Ok(views.html.sectionPreview(app, section, appForm.sections.find(_.sectionNumber == sectionNumber).get,
          opp, fields, answers, controllers.routes.ApplicationController.show(app.id).url, Option(controllers.routes.ApplicationController.resetAndEditSection(app.id, sectionNumber).url)))
      case None => NotFound
    }
  }

  def renderSectionPreviewInProgress(id: ApplicationId, sectionNumber: Int, section: Option[ApplicationSection], fields: Seq[Field]) = {
    val ft = gatherApplicationDetails(id)
    val answers = section.map { s => s.answers }.getOrElse(JsObject(Seq()))

    ft.map {
      case Some((app, appForm, opp)) =>
        Ok(views.html.sectionPreview(app, section, appForm.sections.find(_.sectionNumber == sectionNumber).get, opp, fields, answers, controllers.routes.ApplicationController.editSectionForm(app.id, sectionNumber).url, None))
      case None => NotFound
    }
  }

  type PreviewFunction = (ApplicationOverview, ApplicationForm, Opportunity, Seq[ApplicationSection], Option[String], Map[Int, Seq[forms.Field]]) => Html

  def renderApplicationPreview(id: ApplicationId, preview: PreviewFunction) = {
    val ft = gatherApplicationDetails(id)
    val sections = applications.getSections(id)

    val details = for {
      appDetails <- ft
      sections <- sections
    } yield (appDetails, sections)

    details.map {
      case (Some((app, appForm, opp)), scs) =>
        val title = scs.find(_.sectionNumber == 1).flatMap(s => (s.answers \ "title").validate[String].asOpt)
        Ok(preview(app, appForm, opp, scs.sortBy(_.sectionNumber), title, getFieldMap(appForm)))

      case _ => NotFound
    }
  }

  def getFieldMap(form: ApplicationForm): Map[Int, Seq[Field]] = {
    Map(form.sections.map(sec => sec.sectionNumber -> sec.fields): _*)
  }

  def applicationPreview(id: ApplicationId) = Action.async {
    renderApplicationPreview(id, views.html.applicationPreview.apply)
  }

  def gatherApplicationDetails(id: ApplicationId): Future[Option[(ApplicationOverview, ApplicationForm, Opportunity)]] = {
    for {
      a <- OptionT(applications.overview(id))
      af <- OptionT(appForms.byId(a.applicationFormId))
      o <- OptionT(opps.byId(af.opportunityId))
    } yield (a, af, o)
  }.value

}


