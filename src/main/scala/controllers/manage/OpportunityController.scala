package controllers.manage

import javax.inject.Inject

import actions.OpportunityAction
import cats.data.Validated._
import controllers.FieldCheckHelpers.hinting
import controllers._
import forms._
import forms.validation.DateTimeRangeValues
import models._
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services.{ApplicationFormOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

case class OpportunityLibraryEntry(id: OpportunityId, title: String, status: String, structure: String)

class OpportunityController @Inject()(opportunities: OpportunityOps, appForms: ApplicationFormOps, OpportunityAction: OpportunityAction)(implicit ec: ExecutionContext) extends Controller {

  def showOpportunityPreview(id: OpportunityId, sectionNumber: Option[Int]) = OpportunityAction(id).async { implicit request =>
    appForms.byOpportunityId(id).map {
      case Some(appForm) => Ok(views.html.manage.previewDefaultOpportunity(request.uri, request.opportunity, sectionNumber.getOrElse(1), appForm))
      case None => NotFound
    }
  }

  def showOpportunityPublishPreview(id: OpportunityId, sectionNumber: Option[Int]) = OpportunityAction(id).async { implicit request =>
    appForms.byOpportunityId(id).map {
      case Some(appForm) => Ok(views.html.manage.previewPublishOpportunity(request.uri, request.opportunity, sectionNumber.getOrElse(1), appForm))
      case None => NotFound
    }
  }


  def previewOpportunity(id: OpportunityId, sectionNumber: Option[Int]) = OpportunityAction(id).async { implicit request =>
    appForms.byOpportunityId(id).map {
      case Some(appForm) => Ok(views.html.manage.previewDefaultOpportunity(request.uri, request.opportunity, sectionNumber.getOrElse(1), appForm))
      case None => NotFound
    }
  }

  def showNewOpportunityForm() = Action { request =>
    Ok(views.html.manage.newOpportunityChoice(request.uri))
  }

  def chooseHowToCreateOpportunity(choiceText: Option[String]) = Action { implicit request =>
    CreateOpportunityChoice(choiceText).map {
      case NewOpportunityChoice => Ok(views.html.wip(controllers.manage.routes.OpportunityController.showOpportunityLibrary().url))
      case ReuseOpportunityChoice => Redirect(controllers.manage.routes.OpportunityController.showOpportunityLibrary())
    }.getOrElse(Redirect(controllers.manage.routes.OpportunityController.showNewOpportunityForm()))
  }

  private def libraryEntry(o: Opportunity): OpportunityLibraryEntry = OpportunityLibraryEntry(o.id, o.title, o.statusString, "Responsive, claim, FEC")

  def showOpportunityLibrary = Action.async { request =>
    opportunities.getOpportunitySummaries.map { os => Ok(views.html.manage.showOpportunityLibrary(request.uri, os.map(libraryEntry))) }
  }

  def showOverviewPage(id: OpportunityId) = OpportunityAction(id).async { request =>
    appForms.byOpportunityId(id).map {
      case Some(appForm) => Ok(views.html.manage.opportunityOverview(request.uri, request.opportunity, appForm))
      case None => NotFound
    }
  }

  def viewQuestions(id: OpportunityId, sectionNumber: Int) = OpportunityAction(id).async { request =>
    appForms.byOpportunityId(id).map {
      case Some(appForm) =>
        appForm.sections.find(_.sectionNumber == sectionNumber) match {
          case Some(formSection) => Ok(views.html.manage.viewQuestions(request.uri, request.opportunity, formSection))
          case None => NotFound
        }
      case None => NotFound
    }
  }



  val TITLE_FIELD_NAME = "title"
  val titleField = TextField(label = Some(TITLE_FIELD_NAME), name = TITLE_FIELD_NAME, isNumeric = false, maxWords = 20)
  val titleQuestion = Map(TITLE_FIELD_NAME -> Question("What is your opportunity called ?"))

  def editTitle(id: OpportunityId) = OpportunityAction(id) { request =>
    val answers = JsObject(Seq(TITLE_FIELD_NAME -> Json.toJson(request.opportunity.title)))
    val hints = hinting(answers, Map(titleField.name -> titleField.check))
    Ok(views.html.manage.editTitleForm(titleField, request.opportunity, titleQuestion, answers, Seq(), hints, request.uri))
  }

  def saveTitle(id: OpportunityId) = OpportunityAction(id).async(JsonForm.parser) { implicit request =>
    JsonHelpers.flatten(request.body.values) match {
      case _ => titleField.check(titleField.name, Json.toJson(JsonHelpers.flatten(request.body.values).getOrElse(TITLE_FIELD_NAME, ""))) match {
        case Nil => opportunities.saveSummary(request.opportunity.summary.copy(title = JsonHelpers.flatten(request.body.values).getOrElse(TITLE_FIELD_NAME, ""))).map { _ =>
          request.body.action match {
            case Preview =>
              Redirect(controllers.manage.routes.OpportunityController.previewTitle(id))
                .flashing(PREVIEW_BACK_URL_FLASH -> controllers.manage.routes.OpportunityController.editTitle(id).url)
            case _ =>
              Redirect(controllers.manage.routes.OpportunityController.showOverviewPage(id))

          }

        }
        case errs =>
          val hints = hinting(request.body.values, Map(titleField.name -> titleField.check))
          Future.successful(Ok(views.html.manage.editTitleForm(titleField, request.opportunity, titleQuestion, request.body.values, errs, hints, request.uri))) //hints
      }
    }
  }

  val SECTION_FIELD_NAME = "section"
  val sectionField = TextAreaField(None, SECTION_FIELD_NAME, 500)

  def doEditSection(opp: Opportunity, sectionNum: Int, initial: JsObject, errs: Seq[forms.validation.FieldError] = Nil) = {
    val hints = FieldCheckHelpers.hinting(initial, Map(SECTION_FIELD_NAME -> sectionField.check))
    opp.description.find(_.sectionNumber == sectionNum) match {
      case Some(section) =>
        val q = Question(section.description.getOrElse(""), None, section.helpText)
        Ok(views.html.manage.editOppSectionForm(sectionField, opp, section,
          routes.OpportunityController.editSection(opp.id, sectionNum).url, Map(SECTION_FIELD_NAME -> q), initial, errs, hints))
      case None => NotFound
    }

  }

  def editSection(id: OpportunityId, sectionNum: Int) = OpportunityAction(id).async { request =>
    appForms.byOpportunityId(id).map {
      case Some(appForm) =>
        request.opportunity.description.find(_.sectionNumber == sectionNum) match {
          case Some(sect) if sect.sectionType == OppSectionType.Text =>
            val answers = JsObject(Seq(SECTION_FIELD_NAME -> Json.toJson(sect.text)))
            doEditSection(request.opportunity, sectionNum, answers)
          case Some(sect) => Ok(views.html.manage.whatWeWillAskPreview(request.uri, request.opportunity, sectionNum, appForm))
          case None => NotFound
        }
      case None => NotFound
    }
  }

  def saveSection(id: OpportunityId, sectionNum: Int) = OpportunityAction(id).async(JsonForm.parser) { implicit request =>
    (request.body.values \ SECTION_FIELD_NAME).toOption.map { fValue =>
      sectionField.check(SECTION_FIELD_NAME, fValue) match {
        case Nil =>
          opportunities.saveDescriptionSectionText(id, sectionNum, Some(fValue.as[String])).map { _ =>
            request.body.action match {
              case Preview =>
                Redirect(controllers.manage.routes.OpportunityController.previewOppSection(id, sectionNum))
                  .flashing(PREVIEW_BACK_URL_FLASH ->
                    controllers.manage.routes.OpportunityController.editSection(id, sectionNum).url)
              case _ =>
                Redirect(controllers.manage.routes.OpportunityController.showOverviewPage(id))
            }
          }
        case errors => Future.successful(doEditSection(request.opportunity, sectionNum, request.body.values, errors))
      }
    }.getOrElse(Future.successful(BadRequest))
  }

  def viewTitle(id: OpportunityId) = OpportunityAction(id) { request =>
    request.opportunity.publishedAt match {
      case Some(dateval) => Ok(views.html.manage.viewTitle(request.opportunity))
      case None => Redirect(controllers.manage.routes.OpportunityController.editTitle(id))
    }
  }

  def viewSection(id: OpportunityId, sectionNum: Int) = OpportunityAction(id).async { request =>
    appForms.byOpportunityId(id).map {
      case Some(appForm) => request.opportunity.publishedAt match {
        case Some(_) => Ok(views.html.manage.viewOppSection(request.opportunity, appForm, sectionNum, request.flash.get(PREVIEW_BACK_URL_FLASH)))
        case None => Redirect(controllers.manage.routes.OpportunityController.editSection(id, sectionNum))
      }
      case None => NotFound
    }
  }

  def duplicate(id: OpportunityId) = Action.async {
    request =>
      opportunities.duplicate(id).map {
        case Some(newOppId) => Redirect(controllers.manage.routes.OpportunityController.showOverviewPage(newOppId))
        case None => NotFound
      }
  }

  def publish(id: OpportunityId) = OpportunityAction(id).async {
    request =>
      val emailto = "Portfolio.Manager@rifs.gov.uk"
      val dtf = DateTimeFormat.forPattern("HH:mm:ss")
      opportunities.publish(id).map {
        case Some(dt) =>
          Ok(views.html.manage.publishedOpportunity(request.opportunity.id, emailto, dtf.print(dt)))
        case None => NotFound
      }
  }



  def showPMGuidancePage(backUrl: String) = Action {
    request =>
      Ok(views.html.manage.guidance(backUrl))
  }

  def previewOppSection(id: OpportunityId, sectionid: Int) = OpportunityAction(id) {
    request =>
      Ok(views.html.manage.previewOppSection(request.opportunity, sectionid, request.flash.get(PREVIEW_BACK_URL_FLASH)))
  }


  def previewTitle(id: OpportunityId) = OpportunityAction(id) {
    request =>
      Ok(views.html.manage.previewTitle(request.opportunity, request.flash.get(PREVIEW_BACK_URL_FLASH)))
  }



}

sealed trait CreateOpportunityChoice {
  def name: String
}

object CreateOpportunityChoice {
  def apply(s: Option[String]): Option[CreateOpportunityChoice] = s match {
    case Some(NewOpportunityChoice.name) => Some(NewOpportunityChoice)
    case Some(ReuseOpportunityChoice.name) => Some(ReuseOpportunityChoice)
    case _ => None
  }
}

case object NewOpportunityChoice extends CreateOpportunityChoice {
  val name = "new"
}

case object ReuseOpportunityChoice extends CreateOpportunityChoice {
  val name = "reuse"
}