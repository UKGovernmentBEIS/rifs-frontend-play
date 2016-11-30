package controllers.manage

import javax.inject.Inject

import cats.data.Validated._
import controllers.FieldCheckHelpers.FieldErrors
import controllers.{Complete, FieldCheckHelpers, JsonForm, JsonHelpers}
import forms.validation.{DateTimeRangeValues, FieldError, MandatoryValidator}
import forms.{DateTimeRangeField, DateValues, TextField}
import models._
import org.joda.time.LocalDate
import play.Logger
import play.api.libs.iteratee.Input.Empty
import play.api.libs.json._
import play.api.mvc.Results.NotFound
import play.api.mvc.{Action, Controller, Result}
import services.OpportunityOps

import scala.concurrent.{ExecutionContext, Future}


class OpportunityController @Inject()(opportunities: OpportunityOps)(implicit ec: ExecutionContext) extends Controller {


  def showOpportunityPreview(id: OpportunityId, sectionNumber: Option[Int]) = Action.async {
    opportunities.byId(id).map {
      case Some(o) => Ok(views.html.manage.opportunityPreview(o, sectionNumber.getOrElse(1)))
      case None => NotFound
    }
  }

  def showNewOpportunityForm = Action {
    Ok(views.html.manage.newOpportunityChoice())
  }

  def chooseHowToCreateOpportunity(choiceText: Option[String]) = Action { implicit request =>
    CreateOpportunityChoice(choiceText).map {
      case NewOpportunityChoice => Ok(views.html.wip(routes.OpportunityController.showNewOpportunityForm().url))
      case ReuseOpportunityChoice => Redirect(controllers.manage.routes.OpportunityController.showOpportunityLibrary())
    }.getOrElse(Redirect(controllers.manage.routes.OpportunityController.showNewOpportunityForm()))
  }

  def showOpportunityLibrary = Action.async {
    opportunities.getOpenOpportunitySummaries.map { os => Ok(views.html.manage.showOpportunityLibrary(os)) }
  }

  def showPMGuidancePage = Action {
    Ok(views.html.manage.guidance())
  }

  val deadlinesField = DateTimeRangeField("deadlines", allowPast = false, isEndDateMandatory = false)
  val deadlineQuestions = Map(
    "deadlines.startDate" -> Question("When does the opportunity open?"),
    "deadlines.endDate" -> Question("What is the closing date?")
  )

  val titleField = TextField(label=Some("title"), name="title", isNumeric=false, maxWords=20)
  val titleQuestion = Map("title" -> Question ("What is your opportunity called ?"))

  def editTitle(id: OpportunityId) = Action.async {
    opportunities.byId(id).map {
      case Some(opp) =>
        val answers = JsObject(Seq("title" -> Json.toJson(opp.title)))
        Ok(views.html.manage.editTitleForm(titleField, opp, titleQuestion, answers, Seq(), Seq()))
      case None => NotFound
    }
  }

  def saveTitle(id: OpportunityId) = Action.async(JsonForm.parser) { implicit request =>
    SaveTextField (id, JsonHelpers.flatten (request.body.values), "title", request.body.values)
  }

  //Refactor - move this to a service or action handler ?
  def SaveTextField (id: OpportunityId, answers: Map[String, String], sectionFieldName: String, fieldValues: JsObject): Future[Result] =  {
    opportunities.byId(id).flatMap {
      case Some(opp) =>
        answers match {
          case _ => titleField.check(titleField.name, Json.toJson(answers.get(sectionFieldName).getOrElse(""))) match {
            case Nil => opportunities.saveSummary(opp.summary.copy(title = (answers.get(sectionFieldName).getOrElse("")))).map(_ => Ok(views.html.wip("")))
            case errs => Future.successful(Ok(views.html.manage.editTitleForm(titleField, opp, titleQuestion, fieldValues, errs.toList, Seq())))
          }
        }
      case None => Future.successful(NotFound)
    }
  }


  implicit val dvFmt = Json.format[DateValues]
  implicit val dtrFmt = Json.format[DateTimeRangeValues]

  def editDeadlines(id: OpportunityId) = Action.async {
    opportunities.byId(id).map {
      case Some(opp) =>
        val answers = JsObject(Seq("title" -> Json.toJson(dateTimeRangeValuesFor(opp))))
        Ok(views.html.manage.editDeadlinesForm(deadlinesField, opp, deadlineQuestions, answers, Seq(), Seq()))
      case None => NotFound
    }
  }

  private def dateTimeRangeValuesFor(opp: Opportunity) = {
    val sdv = dateValuesFor(opp.startDate)
    val edv = opp.endDate.map(dateValuesFor)
    DateTimeRangeValues(Some(sdv), edv, edv.map(_ => "yes").orElse(Some("no")))
  }

  private def dateValuesFor(ld: LocalDate) =
    DateValues(Some(ld.getDayOfMonth.toString), Some(ld.getMonthOfYear.toString), Some(ld.getYear.toString))

  def saveDeadlines(id: OpportunityId) = Action.async(JsonForm.parser) { implicit request =>
    opportunities.byId(id).flatMap {
      case Some(opp) =>
        (request.body.values \ "deadlines").validate[DateTimeRangeValues] match {
          case JsSuccess(vs, _) =>
            deadlinesField.validator.validate(deadlinesField.name, vs) match {
              case Valid(v) =>
                val summary = opp.summary.copy(startDate = v.startDate, endDate = v.endDate)
                opportunities.saveSummary(summary).map(_ => Ok(views.html.wip("")))
              case Invalid(errors) =>
                Future.successful(Ok(views.html.manage.editDeadlinesForm(deadlinesField, opp, deadlineQuestions, request.body.values, errors.toList, Seq())))
            }
          case JsError(errors) => Future.successful(BadRequest(errors.toString))
        }
      case None => Future.successful(NotFound)
    }
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