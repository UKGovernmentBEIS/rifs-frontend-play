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

  def applicationPreview(id: ApplicationId) = Action.async {
    println("##########11111111111111")


    val ft = for {
      a <- OptionT(applications.overview(id))
      af <- OptionT(applicationForms.byId(a.applicationFormId))
      opp <- OptionT(opportunities.byId(af.opportunityId))
    } yield (af, a, opp)

    val sections = applications.getSections(id)

    for (is<- sections)
      println("##########"+ is)

    for (ss <- sections) {
      println("##########" + ss)
      //fieldsFor(ss.map(ddd =>ddd.sectionNumber))
    }


   // val t = ft.value.flatMap{ x => applications.getSections(id).map{ y => (x,y)}}
   // val ts = ft.value.flatMap{ x => sections.map{ yd => yd.map(ydd => fieldsFor(ydd.sectionNumber).toSet)  }}
    val y = for {
      aafopp <- ft.value
      ss <- sections
    } yield (aafopp, ss)

//val fff: Seq[ApplicationSection] = sections
 //   val gg = fff.map(a => a.s_.se)


  //  val fieldsa = for (i<- sections) {
  //    println("##########" + i)
 //     i.map(_.sectionNumber).toSet
   // }

    //for (j<- fieldsa)
    //  println("----------fields:-" + j)

//==val dd = y._2.
  // val fieldseq = fieldsFor(1)
   // val pp = for{
      //sectionNumber.map(cc => Map(cc.sectionNumber, fieldsFor(cc.sectionNumber) ))
   // h1<- sections.map(_.map(_.sectionNumber))
   // h2<- sections.map(_.map(_.sectionNumber))
    //h2 <- sectionNumber.map(fieldsFor(_.sectionNumber))
   // } yield (Map(h1,h2))

    def getFieldmap(secs:Seq[ApplicationSection]) : Map[Int, Seq[Field]] = {
      val mp = scala.collection.mutable.Map[Int, Seq[Field]]()
       for(sc <- secs) {
        Map(sc.sectionNumber,  fieldsFor(sc.sectionNumber))
      } //yield (mp)
      ???
    }

      y.map {
      case (Some((form, overview, opp )), ss) => Ok(views.html.applicationPreview(form, overview, opp, ss.sortWith(_.sectionNumber < _.sectionNumber), ss.map(_.sectionNumber).toSet, getFieldmap(ss) ))
      case _ => NotFound
    }
  }

}
