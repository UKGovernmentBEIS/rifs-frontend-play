package controllers.manage

import javax.inject.Inject

import actions.OpportunityAction
import controllers.FieldCheckHelpers.hinting
import controllers.{JsonForm, JsonHelpers, Preview}
import forms.TextField
import models.{OpportunityId, Question}
import play.api.libs.json._
import play.api.mvc._
import services.OpportunityOps

import scala.concurrent.{ExecutionContext, Future}

class TitleController @Inject()(opportunities: OpportunityOps, OpportunityAction: OpportunityAction)(implicit ec: ExecutionContext) extends Controller {

  val titleFieldName = "title"
  val titleField = TextField(label = Some(titleFieldName), name = titleFieldName, isNumeric = false, maxWords = 20)
  val titleQuestion = Map(titleFieldName -> Question("What is your opportunity called ?"))

  def edit(id: OpportunityId) = OpportunityAction(id) { request =>
    val answers = JsObject(Seq(titleFieldName -> Json.toJson(request.opportunity.title)))
    val hints = hinting(answers, Map(titleField.name -> titleField.check))
    Ok(views.html.manage.editTitleForm(titleField, request.opportunity, titleQuestion, answers, Seq(), hints, request.uri))
  }

  def save(id: OpportunityId) = OpportunityAction(id).async(JsonForm.parser) { implicit request =>
    JsonHelpers.flatten(request.body.values) match {
      case _ => titleField.check(titleField.name, Json.toJson(JsonHelpers.flatten(request.body.values).getOrElse(titleFieldName, ""))) match {
        case Nil => opportunities.saveSummary(request.opportunity.summary.copy(title = JsonHelpers.flatten(request.body.values).getOrElse(titleFieldName, ""))).map { _ =>
          request.body.action match {
            case Preview =>
              Redirect(controllers.manage.routes.TitleController.preview(id))
                .flashing(PREVIEW_BACK_URL_FLASH -> controllers.manage.routes.TitleController.edit(id).url)
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

  def view(id: OpportunityId) = OpportunityAction(id) { request =>
    request.opportunity.publishedAt match {
      case Some(dateval) => Ok(views.html.manage.viewTitle(request.opportunity))
      case None => Redirect(controllers.manage.routes.TitleController.edit(id))
    }
  }

  def preview(id: OpportunityId) = OpportunityAction(id) { request =>
      Ok(views.html.manage.previewTitle(request.opportunity, request.flash.get(PREVIEW_BACK_URL_FLASH)))
  }
}
