package controllers.manage

import javax.inject.Inject

import actions.OpportunityAction
import controllers.FieldCheckHelpers.hinting
import models.OpportunityId
import play.api.libs.json._
import play.api.mvc._
import services.OpportunityOps

import scala.concurrent.ExecutionContext

class TitleController @Inject()(
                                 val opportunities: OpportunityOps,
                                 val OpportunityAction: OpportunityAction)
                               (implicit val ec: ExecutionContext)
  extends Controller with TitleSave {

  def edit(id: OpportunityId) = OpportunityAction(id) { request =>
    val answers = JsObject(Seq(fieldName -> Json.toJson(request.opportunity.title)))
    val hints = hinting(answers, Map(field.name -> field.check))
    Ok(views.html.manage.editTitleForm(field, request.opportunity, questions, answers, Seq(), hints, request.uri))
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