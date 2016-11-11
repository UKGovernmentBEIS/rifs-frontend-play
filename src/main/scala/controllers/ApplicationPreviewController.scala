package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import forms.Field
import models._

import play.Logger
import play.api.mvc.{Action, Controller}
import play.twirl.api.Html
import services.{ApplicationFormOps, ApplicationOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

class ApplicationPreviewController @Inject()(applications: ApplicationOps, applicationForms: ApplicationFormOps, opportunities: OpportunityOps)(implicit ec: ExecutionContext)
  extends Controller {

  import ApplicationData._

  def previewSection (id: ApplicationId, sectionNumber: Int) = Action.async { request =>
    fieldsFor(sectionNumber) match {
      case Some(fields) => applications.getSection(id, sectionNumber).flatMap { section =>
        section.flatMap(_.completedAtText) match {
          case None => renderSectionPreviewInProgress (id, sectionNumber, section, fields)
          case _ => renderSectionPreviewCompleted (id, sectionNumber, section, fields)
        }
      }
      case None => Future.successful(Ok(views.html.wip(routes.ApplicationController.show(id).url)))
    }
  }

  def renderSectionPreviewCompleted (id: ApplicationId, sectionNumber: Int, section: Option[ApplicationSection], fields: Seq[Field]) = {
    val ft = gatherApplicationDetails(id)
    val answers = section.map { s => JsonHelpers.flatten("", s.answers) }.getOrElse(Map[String, String]())

    ft.map {
      case Some((app, appForm, opp)) =>
        Ok(views.html.sectionPreview(app, section, appForm.sections.find(_.sectionNumber == sectionNumber).get,
          opp, fields, answers, controllers.routes.ApplicationController.show(app.id).url, Option(controllers.routes.ApplicationController.resetAndEditSection(app.id, sectionNumber).url)))
      case None => NotFound
    }
  }

  def renderSectionPreviewInProgress (id: ApplicationId, sectionNumber: Int, section: Option[ApplicationSection], fields: Seq[Field]) = {
    val ft = gatherApplicationDetails(id)
    val answers = section.map { s => JsonHelpers.flatten("", s.answers) }.getOrElse(Map[String, String]())

    ft.map {
      case Some((app, appForm, opp)) =>
        Ok(views.html.sectionPreview(app, section, appForm.sections.find(_.sectionNumber == sectionNumber).get, opp, fields, answers, controllers.routes.ApplicationController.editSectionForm(app.id, sectionNumber).url, None))
      case None => NotFound
    }
  }

  //this is duplicated from ActionHandler - needs refactoring
  def gatherApplicationDetails(id: ApplicationId): Future[Option[(ApplicationOverview, ApplicationForm, Opportunity)]] = {
    for {
      a <- OptionT(applications.overview(id))
      af <- OptionT(applicationForms.byId(a.applicationFormId))
      o <- OptionT(opportunities.byId(af.opportunityId))
    } yield (a, af, o)
  }.value

  type PreviewFunction = (ApplicationForm, ApplicationOverview, Opportunity, Seq[ApplicationSection], Option[String], Map[Int, Seq[forms.Field]]) => Html

  def renderApplicationPreview(id: ApplicationId, preview: PreviewFunction) = {
    val ft = gatherApplicationDetails(id)
    val sections = applications.getSections(id)

    val details = for {
      appDetails <- ft
      ss <- sections
    } yield (appDetails, ss)

    details.map {
      case (Some((form, overview, o)), scs) =>
        val title = scs.find(_.sectionNumber == 1).flatMap(s => (s.answers \ "title").validate[String].asOpt)
        Ok(preview(overview, form, o, scs.sortBy(_.sectionNumber), title, getFieldMap(scs)))

      case _ => NotFound
    }
  }

  def getFieldMap(secs: Seq[ApplicationSection]): Map[Int, Seq[Field]] = {
    Map(secs.map(sec => sec.sectionNumber -> fieldsFor(sec.sectionNumber).getOrElse(Seq())): _*)
  }

  def applicationPreview(id: ApplicationId) = Action.async {
    renderApplicationPreview(id, views.html.applicationPreview.apply)
  }
}


