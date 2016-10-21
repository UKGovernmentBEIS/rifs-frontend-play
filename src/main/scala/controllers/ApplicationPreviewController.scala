package controllers

import javax.inject.Inject

import cats.data.{NonEmptyList, OptionT}
import cats.instances.future._
import forms.{FieldRule, MandatoryRule, WordCountRule}
import models.{ApplicationFormId, ApplicationId, ApplicationSection}
import play.api.libs.json._
import play.api.mvc.{Action, Controller, Result}
import services.{ApplicationFormOps, ApplicationOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

class ApplicationPreviewController @Inject()(applications: ApplicationOps, applicationForms: ApplicationFormOps, opportunities: OpportunityOps)(implicit ec: ExecutionContext)
  extends Controller
    with ControllerUtils {

  def previewSectionForm(id: ApplicationId, sectionNumber: Int) = Action.async { request =>
    if (sectionNumber == 1) applications.getSection(id, sectionNumber).flatMap { section =>
      previewTitle(id, section)
    }
    else Future.successful(Ok(views.html.wip(routes.ApplicationController.show(id).url)))
  }


  def previewTitle(id: ApplicationId, section: Option[ApplicationSection]) = {
    val ft = for {
      a <- OptionT(applications.byId(id))
      af <- OptionT(applicationForms.byId(a.applicationFormId))
      o <- OptionT(opportunities.byId(af.opportunityId))
    } yield (a, af, o)

    ft.value.map {
      case Some((app, appForm, opp)) => Ok(views.html.previewTitleForm(app, section, appForm.sections.find(_.sectionNumber == 1).get, opp))
      case None => NotFound
    }
  }

}