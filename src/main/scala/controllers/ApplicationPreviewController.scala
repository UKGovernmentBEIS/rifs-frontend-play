package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import forms.Field
import forms.validation.CostItem
import models._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, Controller}
import services.{ApplicationFormOps, ApplicationOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

class ApplicationPreviewController @Inject()(actionHandler: ActionHandler, applications: ApplicationOps, appForms: ApplicationFormOps, opps: OpportunityOps)(implicit ec: ExecutionContext)
  extends Controller {

  implicit val ciReads = Json.reads[CostItem]

  def previewSection(id: ApplicationId, sectionNumber: Int) = Action.async { request =>
    val ft = actionHandler.gatherSectionDetails(id, sectionNumber)

    ft.map {
      case Some(app) =>
        val (backLink, editLink) = app.section.map(_.isComplete) match {
          case Some(true) =>
            (controllers.routes.ApplicationController.show(app.id).url,
              Some(controllers.routes.ApplicationController.resetAndEditSection(app.id, app.formSection.sectionNumber).url))
          case _ =>
            (controllers.routes.ApplicationController.editSectionForm(app.id, app.formSection.sectionNumber).url, None)
        }
        val answers = app.section.map { s => s.answers }.getOrElse(JsObject(List.empty))

        app.formSection.sectionType match {
          case SectionTypeForm =>
            renderSectionPreview(app, app.formSection.fields, answers, backLink, editLink)
          case SectionTypeList =>
            val costItems = app.section.flatMap(s => (s.answers \ "items").validate[List[CostItem]].asOpt).getOrElse(List.empty)
            renderListPreview(app, costItems, answers, backLink, editLink)
        }
      case None => NotFound
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

 /* def viewQuestions_(id: OpportunityId, sectionNumber: Int) = Action.async { request =>
    (for {
      op <- OptionT(opps.byId(id))
      app <- OptionT(appForms.byOpportunityId(id))
    } yield (op, app)
      ).value.map {
      case Some((op, app)) =>
        val section = app.sections.find(_.sectionNumber == sectionNumber)
        section.map { s=> renderviewQuestions(opp, s, s.fields)}.getOrElse(NotFound)
      case None => NotFound
    }
  }*/

  def viewQuestions(opportunityId: OpportunityId,  sectionNumber: Int) = Action.async {
    (for {
      op <- OptionT(opps.byId(opportunityId))
      app <- OptionT(appForms.byOpportunityId(opportunityId))
    } yield (op, app)
      ).value.map {
      case Some((op, app)) =>
      val section = app.sections.find(_.sectionNumber == sectionNumber)
      section.map { s=> renderviewQuestions(op, s, s.fields)}.getOrElse(NotFound)
      case None => NotFound
    }
  }


  def renderviewQuestions(opp: Opportunity, app: ApplicationFormSection, fields: Seq[Field]) = {
    Ok(views.html.manage.viewQuestions(
      opp,
      app,
      fields,
      JsObject(List.empty)))
  }

}


