package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.future._
import forms.Field
import models.{ApplicationId, ApplicationSection}
import play.api.Logger
import play.api.libs.json.JsObject
import play.api.mvc.{Action, Controller}
import services.{ApplicationFormOps, ApplicationOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

class ApplicationPreviewController @Inject()(applications: ApplicationOps, applicationForms: ApplicationFormOps, opportunities: OpportunityOps)(implicit ec: ExecutionContext)
  extends Controller
    with ControllerUtils {

  import ApplicationData._

  def previewSection(id: ApplicationId, sectionNumber: Int) = Action.async { request =>
    fieldsFor(sectionNumber) match {
      case Some(fields) => applications.getSection(id, sectionNumber).flatMap { section =>
        val populatedFields = fields.map { f => f.withValuesFrom(section.map(_.answers).getOrElse(JsObject(Seq()))) }

        previewTitle(id, sectionNumber, section, populatedFields)
      }
      case None => Future.successful(Ok(views.html.wip(routes.ApplicationController.show(id).url)))
    }
  }

  def previewTitle(id: ApplicationId, sectionNumber: Int, section: Option[ApplicationSection], fields: Seq[Field]) = {
    val ft = for {
      a <- OptionT(applications.byId(id))
      af <- OptionT(applicationForms.byId(a.applicationFormId))
      o <- OptionT(opportunities.byId(af.opportunityId))
    } yield (a, af, o)

    ft.value.map {
      case Some((app, appForm, opp)) =>
        Ok(views.html.sectionPreview(app, section, appForm.sections.find(_.sectionNumber == sectionNumber).get, opp, fields))
      case None => NotFound
    }
  }
}
