package controllers

import javax.inject.Inject

import actions.AppSectionAction
import forms.Field
import forms.validation.CostItem
import models._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, Controller}
import services.{ApplicationFormOps, ApplicationOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

class ApplicationPreviewController @Inject()(
                                              actionHandler: ActionHandler,
                                              applications: ApplicationOps,
                                              appForms: ApplicationFormOps,
                                              opps: OpportunityOps,
                                              AppSectionAction: AppSectionAction
                                            )(implicit ec: ExecutionContext)
  extends Controller {

  implicit val ciReads = Json.reads[CostItem]

  def previewSection(id: ApplicationId, sectionNumber: Int) = AppSectionAction(id, sectionNumber) { request =>
    val (backLink, editLink) = request.appSection.section.map(_.isComplete) match {
      case Some(true) =>
        (controllers.routes.ApplicationController.show(request.appSection.id).url,
          Some(controllers.routes.ApplicationController.resetAndEditSection(request.appSection.id, request.appSection.sectionNumber).url))
      case _ =>
        (controllers.routes.ApplicationController.editSectionForm(request.appSection.id, request.appSection.sectionNumber).url, None)
    }
    val answers = request.appSection.section.map { s => s.answers }.getOrElse(JsObject(List.empty))

    request.appSection.formSection.sectionType match {
      case SectionTypeForm =>
        renderSectionPreview(request.appSection, request.appSection.formSection.fields, answers, backLink, editLink)
      case SectionTypeList =>
        val costItems = request.appSection.section.flatMap(s => (s.answers \ "items").validate[List[CostItem]].asOpt).getOrElse(List.empty)
        renderListPreview(request.appSection, costItems, answers, backLink, editLink)
    }
  }

  def renderSectionPreview(app: ApplicationSectionDetail, fields: Seq[Field], answers: JsObject, backLink: String, editLink: Option[String]) = {
    Ok(views.html.sectionPreview(app, fields, answers, backLink, editLink))
  }

  def renderListPreview(app: ApplicationSectionDetail, items: Seq[CostItem], answers: JsObject, backLink: String, editLink: Option[String]) = {
    Ok(views.html.listSectionPreview(app, items, answers, backLink, editLink))
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


