package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import forms.Field
import models.{ApplicationId, ApplicationSection}
import play.api.mvc.{Action, Controller}
import services.{ApplicationFormOps, ApplicationOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

class ApplicationPreviewController @Inject()(applications: ApplicationOps, applicationForms: ApplicationFormOps, opportunities: OpportunityOps)(implicit ec: ExecutionContext)
  extends Controller {

  import ApplicationData._


  //TODO: Tidy the code below presumably a lot of duplication
  def previewSectionCompleted(id: ApplicationId, sectionNumber: Int) = Action.async { request =>
    fieldsFor(sectionNumber) match {
      case Some(fields) => applications.getSection(id, sectionNumber).flatMap { section =>
        renderSectionPreviewCompleted(id, sectionNumber, section, fields)
      }
      case None => Future.successful(Ok(views.html.wip(routes.ApplicationController.show(id).url)))
    }
  }

  def previewSectionInProgress(id: ApplicationId, sectionNumber: Int) = Action.async { request =>
    fieldsFor(sectionNumber) match {
      case Some(fields) => applications.getSection(id, sectionNumber).flatMap { section =>
        renderSectionPreviewInProgress(id, sectionNumber, section, fields)
      }
      case None => Future.successful(Ok(views.html.wip(routes.ApplicationController.show(id).url)))
    }
  }

  def renderSectionPreviewInProgress(id: ApplicationId, sectionNumber: Int, section: Option[ApplicationSection], fields: Seq[Field]) = {
    val ft = for {
      a <- OptionT(applications.byId(id))
      af <- OptionT(applicationForms.byId(a.applicationFormId))
      o <- OptionT(opportunities.byId(af.opportunityId))
    } yield (a, af, o)

    val answers = section.map { s => JsonHelpers.flatten("", s.answers) }.getOrElse(Map[String, String]())

    ft.value.map {
      case Some((app, appForm, opp)) =>
        Ok(views.html.sectionPreviewInProgress(app, section, appForm.sections.find(_.sectionNumber == sectionNumber).get, opp, fields, answers))
      case None => NotFound
    }
  }

  def renderSectionPreviewCompleted(id: ApplicationId, sectionNumber: Int, section: Option[ApplicationSection], fields: Seq[Field]) = {
    val ft = for {
      a <- OptionT(applications.byId(id))
      af <- OptionT(applicationForms.byId(a.applicationFormId))
      o <- OptionT(opportunities.byId(af.opportunityId))
    } yield (a, af, o)

    val answers = section.map { s => JsonHelpers.flatten("", s.answers) }.getOrElse(Map[String, String]())

    ft.value.map {
      case Some((app, appForm, opp)) =>
        Ok(views.html.sectionPreviewCompleted(app, section, appForm.sections.find(_.sectionNumber == sectionNumber).get, opp, fields, answers))
      case None => NotFound
    }
  }
}
