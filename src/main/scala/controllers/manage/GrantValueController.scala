package controllers.manage

import javax.inject.Inject

import actions.OpportunityAction
import models._
import play.api.libs.json._
import play.api.mvc._
import services.OpportunityOps

import scala.concurrent.ExecutionContext

class GrantValueController @Inject()(
                                      val opportunities: OpportunityOps,
                                      val OpportunityAction: OpportunityAction)
                                    (implicit val ec: ExecutionContext)
  extends Controller with GrantValueSave {
  val viewGrantValueFlash = "ViewGrantValueFlash"

  def view(id: OpportunityId) = OpportunityAction(id) { request =>
    if (request.opportunity.isPublished)
      Ok(views.html.manage.viewGrantValue(request.opportunity))
    else
      Redirect(editPage(id))
  }

  def edit(id: OpportunityId) = OpportunityAction(id) { request =>
    request.opportunity.publishedAt match {
      case Some(_) => BadRequest
      case None =>
        val values = JsObject(Seq(fieldName -> JsNumber(request.opportunity.value.amount)))
        Ok(doEdit(request.opportunity, values, Nil))
    }
  }

  def preview(id: OpportunityId) = OpportunityAction(id) { request =>
    Ok(views.html.manage.viewGrantValue(request.opportunity, request.flash.get(PREVIEW_BACK_URL_FLASH)))
  }

}
