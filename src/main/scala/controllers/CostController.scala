package controllers

import javax.inject.Inject

import cats.data.OptionT
import cats.data.Validated.{Invalid, Valid}
import cats.instances.future._
import controllers.FieldCheckHelpers.FieldErrors
import forms.validation.{CostItemValidator, CostItemValues, FieldError}
import models.ApplicationId
import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, Controller, Result}
import services.ApplicationOps

import scala.concurrent.{ExecutionContext, Future}

class CostController @Inject()(actionHandler: ActionHandler, applications: ApplicationOps)(implicit ec: ExecutionContext)
  extends Controller {

  def addItem(applicationId: ApplicationId, sectionNumber: Int) = Action.async { implicit request =>
    showItemForm(applicationId, sectionNumber, Map(), List())
  }

  implicit val costItemValuesR = Json.reads[CostItemValues]
  def post(applicationId: ApplicationId, sectionNumber: Int) = Action.async(JsonForm.parser) { implicit request =>
    Logger.debug(s"Action is ${request.body.action}")
    request.body.action match {
      case Save => Future.successful(Redirect(controllers.routes.ApplicationController.show(applicationId)))
      case Preview =>
        val backLink: String = controllers.routes.ApplicationController.showSectionForm(applicationId, sectionNumber).url
        Future.successful(Redirect(controllers.routes.OpportunityController.wip(backLink)))

      case Complete =>
        applications.getSection(applicationId, sectionNumber).flatMap {
          case Some(section) =>
            (section.answers \ "items").validate[List[CostItemValues]].asOpt.map { items =>
              Logger.debug(s"items are $items")
              val validatedItems = items.map(CostItemValidator.validate("items", _))
              val valids = validatedItems.collect { case Valid(i) => i }
              // ignore for now - should be none anyway if the cost item form did its job
              val errs = validatedItems.collect { case Invalid(es) => es }

              if (valids.map(_.cost).sum <= 2000)
                applications.completeSection(applicationId, sectionNumber, section.answers).map {
                  _ => Redirect(controllers.routes.ApplicationController.show(applicationId))
                }
              else
                actionHandler.redisplaySectionForm(
                  applicationId,
                  sectionNumber,
                  JsonHelpers.flatten("", section.answers),
                  List(FieldError("items", "Total requested exceeds limit. Please check costs of items")))
            }.getOrElse(Future.successful(Redirect(controllers.routes.ApplicationController.show(applicationId))))


          case None => Future.successful(NotFound)
        }
    }
  }

  def postItem(applicationId: ApplicationId, sectionNumber: Int) = Action.async(JsonForm.parser) { implicit request =>
    val fieldValues: JsObject = request.body.values
    applications.saveItem(applicationId, sectionNumber, fieldValues).flatMap {
      case Nil => Future.successful(Redirect(routes.ApplicationController.showSectionForm(applicationId, sectionNumber)))
      case errs => showItemForm(applicationId, sectionNumber, JsonHelpers.flatten("", fieldValues), errs)
    }
  }

  def deleteItem(applicationId: ApplicationId, sectionNumber: Int, itemNumber: Int) = Action.async {
    applications.deleteItem(applicationId, sectionNumber, itemNumber).map { _ =>
      Redirect(controllers.routes.ApplicationController.showSectionForm(applicationId, sectionNumber))
    }
  }

  def showItemForm(applicationId: ApplicationId, sectionNumber: Int, answers: Map[String, String], errs: FieldErrors): Future[Result] = {
    val details1 = actionHandler.gatherApplicationDetails(applicationId)

    val details2 = for {
      ds <- OptionT(details1)
      fs <- OptionT.fromOption[Future](ds._2.sections.find(_.sectionNumber == sectionNumber))
    } yield (ds, fs)

    import ApplicationData._
    val questions = questionsFor(sectionNumber)
    val fields = fieldsFor(sectionNumber).getOrElse(Seq())
    val cancelLink = controllers.routes.ApplicationController.showSectionForm(applicationId, sectionNumber)

    details2.value.map {
      case Some(((overview, form, opp), fs)) => Ok(views.html.costItemForm(overview, form, fs, opp, fields, questions, answers, errs, List(), cancelLink, None))
      case None => NotFound
    }
  }


}
