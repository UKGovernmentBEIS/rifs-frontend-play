package controllers.manage

import javax.inject.Inject

import actions.OpportunityAction
import cats.data.Validated._
import controllers.FieldCheckHelpers.hinting
import controllers._
import forms.validation.DateTimeRangeValues
import forms._
import models._
import org.joda.time.{LocalDate, LocalDateTime}
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services.{ApplicationFormOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

case class OpportunityLibraryEntry(id: OpportunityId, title: String, status: String, structure: String)

class OpportunityController @Inject()(opportunities: OpportunityOps, appForms: ApplicationFormOps, OpportunityAction: OpportunityAction)(implicit ec: ExecutionContext) extends Controller {

  def showOpportunityPreview(id: OpportunityId, sectionNumber: Option[Int]) = OpportunityAction(id) { implicit request =>
    Ok(views.html.manage.opportunityPreview(finalpreview = false, request.uri, request.opportunity, sectionNumber.getOrElse(1)))
  }

  def previewOpportunity(id: OpportunityId, sectionNumber: Option[Int]) = OpportunityAction(id) { implicit request =>
    Ok(views.html.manage.opportunityPreview(finalpreview = true, request.uri, request.opportunity, sectionNumber.getOrElse(1)))
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

  val deadlinesField = DateTimeRangeField("deadlines", allowPast = false, isEndDateMandatory = false)
  val deadlineQuestions = Map(
    "deadlines.startDate" -> Question("When does the opportunity open?"),
    "deadlines.endDate" -> Question("What is the closing date?")
  )

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
        case Nil => opportunities.saveSummary(request.opportunity.summary.copy(title = JsonHelpers.flatten(request.body.values).getOrElse(TITLE_FIELD_NAME, ""))).map{_ =>
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

  def editSection(id: OpportunityId, section: Int) = OpportunityAction(id) { request =>
    request.opportunity.description.find(_.sectionNumber == section) match {
      case Some(sect) =>
        val answers = JsObject(Seq(SECTION_FIELD_NAME -> Json.toJson(sect.text)))
        doEditSection(request.opportunity, section, answers)
      case None => NotFound
    }
  }

  val PREVIEW_BACK_URL_FLASH = "PREVIEW_BACK_URL_FLASH"

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


  def viewGrantValue(id: OpportunityId) = OpportunityAction(id) { request =>
    request.opportunity.publishedAt match {
      case Some(_) => Ok(views.html.manage.viewGrantValue(request.opportunity))
      case None => Redirect(controllers.manage.routes.OpportunityController.editGrantValue(id))
    }
  }

  val GRANT_VALUE_FIELD_NAME = "grantValue"
  val grantValueField = CurrencyField(None, GRANT_VALUE_FIELD_NAME)

  def editGrantValue(id: OpportunityId) = OpportunityAction(id) { request =>
    request.opportunity.publishedAt match {
      case Some(_) => BadRequest
      case None => doEditCostSection( request.opportunity,
                                      JsObject(Seq(GRANT_VALUE_FIELD_NAME -> JsNumber(request.opportunity.value.amount))),
                                      Nil
                                  )
    }
  }

  def doEditCostSection(opp: Opportunity, initial: JsObject, errs: Seq[forms.validation.FieldError]) = {
    val hints = FieldCheckHelpers.hinting(initial, Map(GRANT_VALUE_FIELD_NAME -> grantValueField.check))
    val q = Question("Maximum amount from this opportunity", None, None)

    Ok(views.html.manage.editCostSectionForm(grantValueField, opp,
      routes.OpportunityController.editGrantValue(opp.id).url, Map(GRANT_VALUE_FIELD_NAME -> q), initial, errs, hints))
  }

  def saveGrantValue(id: OpportunityId) = OpportunityAction(id).async(JsonForm.parser) { implicit request =>
    (request.body.values \ GRANT_VALUE_FIELD_NAME).toOption.map { fValue =>
      grantValueField.check(GRANT_VALUE_FIELD_NAME, fValue) match {
        case Nil =>
          val summary = request.opportunity.summary
          opportunities.saveSummary( summary.copy(value = summary.value.copy(amount = fValue.as[BigDecimal])) ).map { _ =>
            request.body.action match {
              case Preview =>
                Redirect(controllers.manage.routes.OpportunityController.previewGrantValue(id))
                  .flashing(PREVIEW_BACK_URL_FLASH ->
                    controllers.manage.routes.OpportunityController.editGrantValue(id).url)
              case _ =>
                Redirect(controllers.manage.routes.OpportunityController.showOverviewPage(id))
            }
          }
        case errors => Future.successful(doEditCostSection(request.opportunity, request.body.values, errors))
      }
    }.getOrElse(Future.successful(BadRequest))
  }

  def previewGrantValue(id: OpportunityId) = OpportunityAction(id) { request =>
    Ok(views.html.manage.viewGrantValue(request.opportunity, request.flash.get(PREVIEW_BACK_URL_FLASH)))
  }

  def viewSection(id: OpportunityId, sectionNum: Int) = OpportunityAction(id) { request =>
    request.opportunity.publishedAt match {
      case Some(_) => Ok(views.html.manage.viewOppSection(request.opportunity, sectionNum, request.flash.get(PREVIEW_BACK_URL_FLASH)))
      case None => Redirect(controllers.manage.routes.OpportunityController.editSection(id, sectionNum))
    }
  }

  def duplicate(id: OpportunityId) = Action.async { request =>
    opportunities.duplicate(id).map {
      case Some(newOppId) => Redirect(controllers.manage.routes.OpportunityController.showOverviewPage(newOppId))
      case None => NotFound
    }
  }

  def publish(id: OpportunityId) = OpportunityAction(id).async { request =>
    val emailto = "Portfolio.Manager@rifs.gov.uk"
    val dtf = DateTimeFormat.forPattern("HH:mm:ss")
    opportunities.publish(id).map {
      case Some(dt) =>
        Ok(views.html.manage.submitOpportunity(request.opportunity.id, emailto, dtf.print(dt)))
      case None => NotFound
    }
  }

  val DEADLINES_FIELD_NAME = "deadlines"

  def viewDeadlines(id: OpportunityId) = OpportunityAction(id) { request =>
    val answers = JsObject(Seq(DEADLINES_FIELD_NAME -> Json.toJson(dateTimeRangeValuesFor(request.opportunity))))
    request.opportunity.publishedAt match {
      case Some(dateval) => Ok(views.html.manage.viewDeadlines(deadlinesField, request.opportunity, deadlineQuestions, answers))
      case None => Redirect(controllers.manage.routes.OpportunityController.editDeadlines(id))
    }
  }

  implicit val dvFmt = Json.format[DateValues]
  implicit val dtrFmt = Json.format[DateTimeRangeValues]

  def editDeadlines(id: OpportunityId) = OpportunityAction(id) { request =>
    val answers = JsObject(Seq(DEADLINES_FIELD_NAME -> Json.toJson(dateTimeRangeValuesFor(request.opportunity))))
    Ok(views.html.manage.editDeadlinesForm(deadlinesField, request.opportunity, deadlineQuestions, answers, Seq(), Seq()))
  }

  def saveDeadlines(id: OpportunityId) = OpportunityAction(id).async(JsonForm.parser) { implicit request =>
    (request.body.values \ DEADLINES_FIELD_NAME).validate[DateTimeRangeValues] match {
      case JsSuccess(vs, _) =>
        deadlinesField.validator.validate(deadlinesField.name, vs) match {
          case Valid(v) =>
            val summary = request.opportunity.summary.copy(startDate = v.startDate, endDate = v.endDate)
            opportunities.saveSummary(summary).map{_ => request.body.action match {
              case Preview =>
                Redirect( controllers.manage.routes.OpportunityController.previewDeadlines(id) )
                  .flashing(PREVIEW_BACK_URL_FLASH -> controllers.manage.routes.OpportunityController.editDeadlines(id).url)
              case _ =>
                Redirect(controllers.manage.routes.OpportunityController.showOverviewPage(id) )
              }
            }
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
    Ok(views.html.manage.previewOppSection(request.opportunity, sectionid, request.flash.get(PREVIEW_BACK_URL_FLASH)))
  }

  private def dateTimeRangeValuesFor(opp: Opportunity) = {
    val sdv = dateValuesFor(opp.startDate)
    val edv = opp.endDate.map(dateValuesFor)
    DateTimeRangeValues(Some(sdv), edv, edv.map(_ => "yes").orElse(Some("no")))
  }

  private def dateValuesFor(ld: LocalDate) =
    DateValues(Some(ld.getDayOfMonth.toString), Some(ld.getMonthOfYear.toString), Some(ld.getYear.toString))

  def previewTitle(id: OpportunityId) = OpportunityAction(id) { request =>
    Ok(views.html.manage.previewTitle(request.opportunity, request.flash.get(PREVIEW_BACK_URL_FLASH)))
  }

  def previewDeadlines(id: OpportunityId) = OpportunityAction(id) { request =>
    val answers = JsObject(Seq(DEADLINES_FIELD_NAME -> Json.toJson(dateTimeRangeValuesFor(request.opportunity))))
    Ok(views.html.manage.previewDeadlines(deadlinesField, request.opportunity, deadlineQuestions, answers, request.flash.get(PREVIEW_BACK_URL_FLASH)))
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