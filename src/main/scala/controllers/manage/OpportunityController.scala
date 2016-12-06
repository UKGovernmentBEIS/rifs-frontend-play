package controllers.manage

import javax.inject.Inject

import actions.OpportunityAction
import cats.data.Validated._
import controllers.FieldCheckHelpers.hinting
import controllers._
import forms.validation.DateTimeRangeValues
import forms.{DateTimeRangeField, DateValues, TextAreaField, TextField}
import models._
import org.joda.time.LocalDate
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services.{ApplicationFormOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

case class OpportunityLibraryEntry(id: OpportunityId, title: String, status: String, structure: String)

class OpportunityController @Inject()(opportunities: OpportunityOps, appForms: ApplicationFormOps, OpportunityAction: OpportunityAction)(implicit ec: ExecutionContext) extends Controller {

  def showOpportunityPreview(id: OpportunityId, sectionNumber: Option[Int]) = OpportunityAction(id) { implicit request =>
    Ok(views.html.manage.opportunityPreview(request.uri, request.opportunity, sectionNumber.getOrElse(1)))
  }

  def showNewOpportunityForm() = Action { request =>
    Ok(views.html.manage.newOpportunityChoice(request.uri))
  }

  def chooseHowToCreateOpportunity(choiceText: Option[String]) = Action { implicit request =>
    CreateOpportunityChoice(choiceText).map {
      case NewOpportunityChoice => Ok(views.html.wip(routes.OpportunityController.showNewOpportunityForm().url))
      case ReuseOpportunityChoice => Redirect(controllers.manage.routes.OpportunityController.showOpportunityLibrary())
    }.getOrElse(Redirect(controllers.manage.routes.OpportunityController.showNewOpportunityForm()))
  }


  private def libraryEntry(o: Opportunity): OpportunityLibraryEntry = {
    val status = o.publishedAt.map(_ => "Open").getOrElse("Unpublished")
    OpportunityLibraryEntry(o.id, o.title, status, "Responsive, claim, FEC")
  }

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

  val deadlinesField = DateTimeRangeField("deadlines", allowPast = false, isEndDateMandatory = false)
  val deadlineQuestions = Map(
    "deadlines.startDate" -> Question("When does the opportunity open?"),
    "deadlines.endDate" -> Question("What is the closing date?")
  )

  val titleField = TextField(label = Some("title"), name = "title", isNumeric = false, maxWords = 20)
  val titleQuestion = Map("title" -> Question("What is your opportunity called ?"))

  def editTitle(id: OpportunityId) = OpportunityAction(id) { request =>
    val answers = JsObject(Seq("title" -> Json.toJson(request.opportunity.title)))
    val hints = hinting (answers, Map(titleField.name -> titleField.check))
    Ok(views.html.manage.editTitleForm(titleField, request.opportunity, titleQuestion, answers, Seq(), hints))
  }

  def saveTitle(id: OpportunityId) = OpportunityAction(id).async(JsonForm.parser) { implicit request =>
    JsonHelpers.flatten(request.body.values) match {
      case _ => titleField.check(titleField.name, Json.toJson(JsonHelpers.flatten(request.body.values).getOrElse("title", ""))) match {
        case Nil => opportunities.saveSummary(request.opportunity.summary.copy(title = JsonHelpers.flatten(request.body.values).getOrElse("title", ""))).map(_ => Ok(views.html.wip("")))
        case errs =>
          val hints = hinting(request.body.values, Map(titleField.name -> titleField.check))
          Future.successful(Ok(views.html.manage.editTitleForm(titleField, request.opportunity, titleQuestion, request.body.values, errs, hints))) //hints
      }
    }
  }

  val DESCRIPTION = "description"
  val descriptionField = TextAreaField(Some(DESCRIPTION), DESCRIPTION, 501)
  val descriptionQuestions = Map(DESCRIPTION ->
    Question("Be as specific as possible so that applicants fully understand the aim of the opportunity." +
      " This will help ensure that applications meet the criteria and objectives.")
  )

  def doEditDescription(opp: Opportunity, section: Int, initial: JsObject, errs: Seq[forms.validation.FieldError] = Nil) = {
    val hints = FieldCheckHelpers.hinting(initial, Map(DESCRIPTION -> descriptionField.check))
    Ok(views.html.manage.editDescriptionForm(descriptionField, opp, section,
      routes.OpportunityController.editDescription(opp.id, section).url,
      descriptionQuestions, initial, errs, hints))
  }

  def editDescription(id: OpportunityId, section: Int) = OpportunityAction(id) { request =>
    request.opportunity.description.find(_.sectionNumber == section) match {
      case Some(sect) =>
        val answers = JsObject(Seq(DESCRIPTION -> Json.toJson(sect.text)))
        doEditDescription(request.opportunity, section, answers)
      case None => NotFound
    }
  }

  val VIEW_OPP_SECTION_FLASH = "VIEW_OPP_SECTION_BACK_URL"

  def saveDescription(id: OpportunityId, section: Int) = OpportunityAction(id).async(JsonForm.parser) { implicit request =>
    (request.body.values \ DESCRIPTION).toOption.map { fValue =>
      descriptionField.check(DESCRIPTION, fValue) match {
        case Nil =>
          opportunities.saveDescriptionSectionText(id, section, Some(fValue.as[String])).map { _ =>
            request.body.action match {
              case Save =>
                Redirect(controllers.manage.routes.OpportunityController.showOverviewPage(id))
              case Preview =>
                Redirect(controllers.manage.routes.OpportunityController.viewOppSection(id, section))
                  .flashing(VIEW_OPP_SECTION_FLASH ->
                    controllers.manage.routes.OpportunityController.editDescription(id, section).url)
            }
          }.recover {
            case e =>
              val errs = Seq(forms.validation.FieldError("", e.getMessage))
              doEditDescription(request.opportunity, section, request.body.values, errs)
          }
        case errors =>
          Future {
            doEditDescription(request.opportunity, section, request.body.values, errors)
          }
      }
    }.getOrElse(Future.successful(BadRequest))
  }

  def viewTitle(id: OpportunityId) = OpportunityAction(id) { request =>
    Ok(views.html.manage.viewTitle(request.opportunity))
  }

  def viewDescription(id: OpportunityId) = OpportunityAction(id) { request =>
    Ok(views.html.manage.viewDescription(request.opportunity))
  }

  def viewGrantValue(id: OpportunityId) = OpportunityAction(id) { request =>
    Ok(views.html.manage.viewGrantValue(request.opportunity))
  }

  def viewOppSection(id: OpportunityId, sectionNum: Int) = OpportunityAction(id) { request =>
    Ok(views.html.manage.viewOppSection(request.opportunity, sectionNum, request.flash.get(VIEW_OPP_SECTION_FLASH)))
  }

  def duplicate(id: OpportunityId) = Action.async { request =>
    opportunities.duplicate(id).map {
      case Some(newOppId) => Redirect(controllers.manage.routes.OpportunityController.showOverviewPage(newOppId))
      case None => NotFound
    }
  }

  def viewDeadlines(id: OpportunityId) = OpportunityAction(id) { request =>
    val answers = JsObject(Seq("deadlines" -> Json.toJson(dateTimeRangeValuesFor(request.opportunity))))
    Ok(views.html.manage.viewDeadlines(deadlinesField, request.opportunity, deadlineQuestions, answers))
  }

  implicit val dvFmt = Json.format[DateValues]
  implicit val dtrFmt = Json.format[DateTimeRangeValues]

  def editDeadlines(id: OpportunityId) = OpportunityAction(id) { request =>
    val answers = JsObject(Seq("deadlines" -> Json.toJson(dateTimeRangeValuesFor(request.opportunity))))
    Ok(views.html.manage.editDeadlinesForm(deadlinesField, request.opportunity, deadlineQuestions, answers, Seq(), Seq()))
  }

  def saveDeadlines(id: OpportunityId) = OpportunityAction(id).async(JsonForm.parser) { implicit request =>
    (request.body.values \ "deadlines").validate[DateTimeRangeValues] match {
      case JsSuccess(vs, _) =>
        deadlinesField.validator.validate(deadlinesField.name, vs) match {
          case Valid(v) =>
            val summary = request.opportunity.summary.copy(startDate = v.startDate, endDate = v.endDate)
            opportunities.saveSummary(summary).map(_ => Redirect(controllers.manage.routes.OpportunityController.showOverviewPage(id)))
          case Invalid(errors) =>
            Future.successful(Ok(views.html.manage.editDeadlinesForm(deadlinesField, request.opportunity, deadlineQuestions, request.body.values, errors.toList, Seq())))
        }
      case JsError(errors) => Future.successful(BadRequest(errors.toString))
    }
  }

  def showPMGuidancePage(backUrl: String) = Action { request =>
    Ok(views.html.manage.guidance(backUrl))
  }

  def previewOppSection(id: OpportunityId, sectionid: Int) = OpportunityAction(id) { request =>
    Ok(views.html.manage.previewOppSection(request.opportunity, sectionid))
  }

  private def dateTimeRangeValuesFor(opp: Opportunity) = {
    val sdv = dateValuesFor(opp.startDate)
    val edv = opp.endDate.map(dateValuesFor)
    DateTimeRangeValues(Some(sdv), edv, edv.map(_ => "yes").orElse(Some("no")))
  }

  private def dateValuesFor(ld: LocalDate) =
    DateValues(Some(ld.getDayOfMonth.toString), Some(ld.getMonthOfYear.toString), Some(ld.getYear.toString))

  def previewTitle(id: OpportunityId) = OpportunityAction(id) { request =>
    Ok(views.html.manage.previewTitle(request.opportunity))
  }

  def previewDescription(id: OpportunityId) = OpportunityAction(id) { request =>
    Ok(views.html.manage.previewDescription(request.opportunity))
  }

  def previewDeadlines(id: OpportunityId) = OpportunityAction(id) { request =>
    val answers = JsObject(Seq("deadlines" -> Json.toJson(dateTimeRangeValuesFor(request.opportunity))))
    Ok(views.html.manage.previewDeadlines(deadlinesField, request.opportunity, deadlineQuestions, answers))
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