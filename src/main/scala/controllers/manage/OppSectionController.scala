package controllers.manage

import javax.inject.Inject

import actions.OpportunityAction
import controllers.{FieldCheckHelpers, JsonForm, Preview}
import forms.TextAreaField
import models.{OppSectionType, Opportunity, OpportunityId, Question}
import play.api.libs.json._
import play.api.mvc._
import services.{ApplicationFormOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

class OppSectionController @Inject()(appForms: ApplicationFormOps,opportunities: OpportunityOps, OpportunityAction: OpportunityAction)(implicit ec: ExecutionContext) extends Controller {
  val SECTION_FIELD_NAME = "section"
  val sectionField = TextAreaField(None, SECTION_FIELD_NAME, 500)

  def doEdit(opp: Opportunity, sectionNum: Int, initial: JsObject, errs: Seq[forms.validation.FieldError] = Nil) = {
    val hints = FieldCheckHelpers.hinting(initial, Map(SECTION_FIELD_NAME -> sectionField.check))
    opp.description.find(_.sectionNumber == sectionNum) match {
      case Some(section) =>
        val q = Question(section.description.getOrElse(""), None, section.helpText)
        Ok(views.html.manage.editOppSectionForm(sectionField, opp, section,
          routes.OppSectionController.edit(opp.id, sectionNum).url, Map(SECTION_FIELD_NAME -> q), initial, errs, hints))
      case None => NotFound
    }
  }

  def edit(id: OpportunityId, sectionNum: Int) = OpportunityAction(id).async { request =>
    appForms.byOpportunityId(id).map {
      case Some(appForm) =>
        request.opportunity.description.find(_.sectionNumber == sectionNum) match {
          case Some(sect) if sect.sectionType == OppSectionType.Text =>
            val answers = JsObject(Seq(SECTION_FIELD_NAME -> Json.toJson(sect.text)))
            doEdit(request.opportunity, sectionNum, answers)
          case Some(sect) => Ok(views.html.manage.whatWeWillAskPreview(request.uri, request.opportunity, sectionNum, appForm))
          case None => NotFound
        }
      case None => NotFound
    }
  }

  def save(id: OpportunityId, sectionNum: Int) = OpportunityAction(id).async(JsonForm.parser) { implicit request =>
    (request.body.values \ SECTION_FIELD_NAME).toOption.map { fValue =>
      sectionField.check(SECTION_FIELD_NAME, fValue) match {
        case Nil =>
          opportunities.saveDescriptionSectionText(id, sectionNum, Some(fValue.as[String])).map { _ =>
            request.body.action match {
              case Preview =>
                Redirect(controllers.manage.routes.OppSectionController.preview(id, sectionNum))
                  .flashing(PREVIEW_BACK_URL_FLASH ->
                    controllers.manage.routes.OppSectionController.edit(id, sectionNum).url)
              case _ =>
                Redirect(controllers.manage.routes.OpportunityController.showOverviewPage(id))
            }
          }
        case errors => Future.successful(doEdit(request.opportunity, sectionNum, request.body.values, errors))
      }
    }.getOrElse(Future.successful(BadRequest))
  }

  def view(id: OpportunityId, sectionNum: Int) = OpportunityAction(id).async { request =>
    appForms.byOpportunityId(id).map {
      case Some(appForm) => request.opportunity.publishedAt match {
        case Some(_) => Ok(views.html.manage.viewOppSection(request.opportunity, appForm, sectionNum, request.flash.get(PREVIEW_BACK_URL_FLASH)))
        case None => Redirect(controllers.manage.routes.OppSectionController.edit(id, sectionNum))
      }
      case None => NotFound
    }
  }
  def preview(id: OpportunityId, sectionid: Int) = OpportunityAction(id) { request =>
      Ok(views.html.manage.previewOppSection(request.opportunity, sectionid, request.flash.get(PREVIEW_BACK_URL_FLASH)))
  }
}
