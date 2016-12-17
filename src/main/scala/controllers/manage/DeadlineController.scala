package controllers.manage

import javax.inject.Inject

import actions.OpportunityAction
import forms.DateValues
import forms.validation.DateTimeRangeValues
import models.{Opportunity, OpportunityId}
import org.joda.time.LocalDate
import play.api.libs.json._
import play.api.mvc._
import services.OpportunityOps

import scala.concurrent.ExecutionContext

class DeadlineController @Inject()(
                                    val opportunities: OpportunityOps,
                                    val OpportunityAction: OpportunityAction)
                                  (implicit val ec: ExecutionContext)
  extends Controller with DeadlineSave {

  private def dateTimeRangeValuesFor(opp: Opportunity) = {
    val sdv = dateValuesFor(opp.startDate)
    val edv = opp.endDate.map(dateValuesFor)
    DateTimeRangeValues(Some(sdv), edv, edv.map(_ => "yes").orElse(Some("no")))
  }

  private def dateValuesFor(ld: LocalDate) =
    DateValues(Some(ld.getDayOfMonth.toString), Some(ld.getMonthOfYear.toString), Some(ld.getYear.toString))


  def view(id: OpportunityId) = OpportunityAction(id) { request =>
    val answers = JsObject(Seq(fieldName -> Json.toJson(dateTimeRangeValuesFor(request.opportunity))))
    if (request.opportunity.isPublished)
      Ok(views.html.manage.viewDeadlines(field, request.opportunity, questions, answers))
    else
      Redirect(editPage(id))
  }

  def edit(id: OpportunityId) = OpportunityAction(id) { request =>
    val answers = JsObject(Seq(fieldName -> Json.toJson(dateTimeRangeValuesFor(request.opportunity))))
    Ok(views.html.manage.editDeadlinesForm(field, request.opportunity, questions, answers, Seq(), Seq()))
  }

  def preview(id: OpportunityId) = OpportunityAction(id) {
    request =>
      val answers = JsObject(Seq(fieldName -> Json.toJson(dateTimeRangeValuesFor(request.opportunity))))
      Ok(views.html.manage.previewDeadlines(field, request.opportunity, questions, answers, request.flash.get(PREVIEW_BACK_URL_FLASH)))
  }
}
