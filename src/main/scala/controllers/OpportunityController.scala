package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.data.Validated._
import cats.instances.future._
import forms.validation.DateTimeRangeValues
import forms.{DateTimeRangeField, DateValues}
import models.{Opportunity, OpportunityId, Question}
import org.joda.time.LocalDate
import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}
import models.{ApplicationId, OpportunityId}
import play.api.mvc.{Action, Controller}
import services.{ApplicationFormOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

class OpportunityController @Inject()(opportunities: OpportunityOps, applications: ApplicationFormOps)(implicit ec: ExecutionContext) extends Controller {
  def showOpportunities = Action.async {
    opportunities.getOpenOpportunitySummaries.map { os => Ok(views.html.showOpportunities(os)) }
  }

  def showOpportunity(id: OpportunityId, sectionNumber: Option[Int]) = Action.async {
    // Make the `Future` calls outside the `for` comprehension to allow them to run
    // concurrently. Could use `Cartesian` to give applicative behaviour (`(f1 |@| f2)`) but
    // IntelliJ doesn't handle it well at the moment.
    val f1 = OptionT(opportunities.byId(id))
    val f2 = OptionT(applications.byOpportunityId(id))

    (for (o <- f1; a <- f2) yield (o, a)).value.map {
      case Some((o, a)) => Ok(views.html.showOpportunity(a.id, o, sectionNumber.getOrElse(1)))
      case None => NotFound
    }
  }

  def showOpportunityPreview(id: OpportunityId, sectionNumber: Option[Int]) = Action.async {

    opportunities.byId(id).map {
      case Some(o) => Ok(views.html.opportunityPreview(o, sectionNumber.getOrElse(1)))
      case None => NotFound
    }
  }

  def showNewOpportunityForm = Action {
    Ok(views.html.newOpportunityChoice())
  }

  def chooseHowToCreateOpportunity(choiceText: Option[String]) = Action { implicit request =>
    CreateOpportunityChoice(choiceText).map {
      case NewOpportunityChoice => Ok(views.html.wip(routes.OpportunityController.showNewOpportunityForm().url))
      case ReuseOpportunityChoice => Redirect(controllers.routes.OpportunityController.showOpportunityLibrary())
    }.getOrElse(Redirect(controllers.routes.OpportunityController.showNewOpportunityForm()))
  }

  def viewTitle(id:OpportunityId) = Action.async { request =>
    val fopp = opportunities.byId(id)
    fopp.map {
      case Some(opp) =>
        Ok(views.html.manage.viewTitle(opp))
      case None => NotFound
    }
  }


  def viewDeadlines(id:OpportunityId) = Action.async { request =>
    val fopp = opportunities.byId(id)
    fopp.map {
      case Some(opp) =>
        Ok(views.html.manage.viewDeadlines(opp))
      case None => NotFound
    }
  }


  def viewDescription(id:OpportunityId) = Action.async { request =>
    val fopp = opportunities.byId(id)
    fopp.map {
      case Some(opp) =>
        Ok(views.html.manage.viewDescription(opp))
      case None => NotFound
    }
  }

  def viewGrantValue(id:OpportunityId) = Action.async { request =>
    val fopp = opportunities.byId(id)
    fopp.map {
      case Some(opp) =>
        Ok(views.html.manage.viewGrantValue(opp))
      case None => NotFound
    }
  }

  def viewOppSection(id:OpportunityId, oppSection:Int) = Action.async { request =>
    val fopp = opportunities.byId(id)
    fopp.map {
      case Some(opp) =>
        Ok(views.html.manage.viewOppSection(opp, oppSection))
      case None => NotFound
    }
  }

  def showOpportunityLibrary = Action.async {
    opportunities.getOpenOpportunitySummaries.map { os => Ok(views.html.showOpportunityLibrary(os)) }
  }

  def showGuidancePage(id: OpportunityId) = Action {
    Ok(views.html.guidance(id))
  }

  def showPMGuidancePage = Action {
    Ok(views.html.manage.guidance())
  }

  def wip(backUrl: String) = Action {
    Ok(views.html.wip(backUrl))
  }

  val deadlinesField = DateTimeRangeField("deadlines", allowPast = false, isEndDateMandatory = false)
  val deadlineQuestions = Map(
    "deadlines.startDate" -> Question("When does the opportunity open?"),
    "deadlines.endDate" -> Question("What is the closing date?")
  )


  implicit val dvFmt = Json.format[DateValues]
  implicit val dtrFmt = Json.format[DateTimeRangeValues]

  def editDeadlines(id: OpportunityId) = Action.async {
    opportunities.byId(id).map {
      case Some(opp) =>
        val answers = JsObject(Seq("deadlines" -> Json.toJson(dateTimeRangeValuesFor(opp))))
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

  def showOpportunitySetupGuidance(id: OpportunityId) = showPMGuidancePage

  def showOverviewPage(opportunityId: OpportunityId) = Action.async {
    val appId = ApplicationId(1)  //we take 1st Application as sample

    (for {
      op <- OptionT(opportunities.byId(opportunityId))
      app <- OptionT(applications.byOpportunityId(opportunityId))
    } yield (op, app)
      ).value.map {
        case Some((op, app)) => Ok(views.html.manage.previewOpportunity(appId, op, app) )
        case None => NotFound
      }
  }

  def duplicate(opportunityId: OpportunityId) = Action.async {
      OptionT(opportunities.byId(opportunityId)).value.map {
        case Some(op) => Ok(views.html.wip( controllers.routes.OpportunityController.showOverviewPage(opportunityId).url ) )
        case None => NotFound
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