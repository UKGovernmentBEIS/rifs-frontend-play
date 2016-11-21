package controllers

import javax.inject.Inject

import forms.Field
import models._
import play.api.libs.json.JsObject
import play.api.mvc.{Action, Controller}
import services.{ApplicationFormOps, ApplicationOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

class ApplicationPreviewController @Inject()(actionHandler: ActionHandler, applications: ApplicationOps, appForms: ApplicationFormOps, opps: OpportunityOps)(implicit ec: ExecutionContext)
  extends Controller {

  def previewSection(id: ApplicationId, sectionNumber: Int) = Action.async { request =>
    val ft = actionHandler.gatherSectionDetails(id, sectionNumber)

    ft.map {
      case Some((app, formSection, section)) =>
        section.map(_.isComplete) match {
          case Some(true) => renderSectionPreviewCompleted(app, formSection, section, formSection.fields)
          case _ => renderSectionPreviewInProgress(app, formSection, section, formSection.fields)
        }
      case None => NotFound
    }
  }

  def renderSectionPreviewCompleted(app: ApplicationDetail, formSection: ApplicationFormSection, section: Option[ApplicationSection], fields: Seq[Field]) = {
    val answers = section.map { s => s.answers }.getOrElse(JsObject(List.empty))

    Ok(views.html.sectionPreview(
      app,
      section,
      formSection,
      fields,
      answers,
      controllers.routes.ApplicationController.show(app.id).url,
      Some(controllers.routes.ApplicationController.resetAndEditSection(app.id, formSection.sectionNumber).url)))
  }

  def renderSectionPreviewInProgress(app: ApplicationDetail, formSection: ApplicationFormSection, section: Option[ApplicationSection], fields: Seq[Field]) = {
    val answers = section.map { s => s.answers }.getOrElse(JsObject(List.empty))
    Ok(views.html.sectionPreview(
      app,
      section,
      formSection,
      fields,
      answers,
      controllers.routes.ApplicationController.editSectionForm(app.id, formSection.sectionNumber).url,
      None))
  }

  def getFieldMap(form: ApplicationForm): Map[Int, Seq[Field]] = {
    Map(form.sections.map(sec => sec.sectionNumber -> sec.fields): _*)
  }

  def applicationPreview(id: ApplicationId) = Action.async {
    gatherApplicationDetails(id).map {
      case Some(app) =>
        val title = app.sections.find(_.sectionNumber == 1).flatMap(s => (s.answers \ "title").validate[String].asOpt)
        Ok(views.html.applicationPreview(app, app.sections.sortBy(_.sectionNumber), title, getFieldMap(app.applicationForm)))

      case _ => NotFound
    }
  }

  def gatherApplicationDetails(id: ApplicationId): Future[Option[ApplicationDetail]] = applications.detail(id)

}


