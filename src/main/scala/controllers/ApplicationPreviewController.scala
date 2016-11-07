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

  def previewSection(id: ApplicationId, sectionNumber: Int) = Action.async { request =>
    fieldsFor(sectionNumber) match {
      case Some(fields) => applications.getSection(id, sectionNumber).flatMap { section =>
        renderSectionPreview(id, sectionNumber, section, fields)
      }
      case None => Future.successful(Ok(views.html.wip(routes.ApplicationController.show(id).url)))
    }
  }

  def renderSectionPreview(id: ApplicationId, sectionNumber: Int, section: Option[ApplicationSection], fields: Seq[Field]) = {
    val ft = for {
      a <- OptionT(applications.byId(id))
      af <- OptionT(applicationForms.byId(a.applicationFormId))
      o <- OptionT(opportunities.byId(af.opportunityId))
    } yield (a, af, o)

    val answers = section.map { s => JsonHelpers.flatten("", s.answers) }.getOrElse(Map[String, String]())

    ft.value.map {
      case Some((app, appForm, opp)) =>
        Ok(views.html.sectionPreview(app, section, appForm.sections.find(_.sectionNumber == sectionNumber).get, opp, fields, answers))
      case None => NotFound
    }
  }

  def renderApplicationPreview(id: ApplicationId, isprintpreview: Boolean) = {
    val ft = for {
      a <- OptionT(applications.overview(id))
      af <- OptionT(applicationForms.byId(a.applicationFormId))
      opp <- OptionT(opportunities.byId(af.opportunityId))
    } yield (af, a, opp)

    val sections = applications.getSections(id)

    val y = for {
      aafopp <- ft.value
      ss <- sections
    } yield (aafopp, ss)

    y.map {
      case (Some((form, overview, opp )), scs) =>
          if(isprintpreview)
            Ok(views.html.applicationPrintPreview(form, overview, opp,
              scs.sortWith(_.sectionNumber < _.sectionNumber), getFieldMap(scs) ))
        else
            Ok(views.html.applicationPreview(form, overview, opp,
              scs.sortWith(_.sectionNumber < _.sectionNumber), getFieldMap(scs) ))
      case _ => NotFound
    }
  }

  def getFieldMap(secs:Seq[ApplicationSection]) : Map[Int, Seq[Field]] = {
    Map(secs.map(sec => sec.sectionNumber -> fieldsFor(sec.sectionNumber).getOrElse(Seq())):_*)
  }

  def applicationPreview(id: ApplicationId) = Action.async {
    renderApplicationPreview(id, false)
  }

  def applicationPrintPreview(id: ApplicationId) = Action.async {
    renderApplicationPreview(id, true)
  }


}
