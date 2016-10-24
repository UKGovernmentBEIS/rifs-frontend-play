package controllers

import javax.inject.Inject

import cats.data.{NonEmptyList, OptionT}
import cats.instances.future._
import forms._
import models.{ApplicationFormId, ApplicationFormSection, ApplicationId, ApplicationSection}
import play.api.libs.json._
import play.api.mvc.{Action, Controller, Result}
import services.{ApplicationFormOps, ApplicationOps, OpportunityOps}

import scala.concurrent.{ExecutionContext, Future}

class ApplicationController @Inject()(applications: ApplicationOps, applicationForms: ApplicationFormOps, opportunities: OpportunityOps)(implicit ec: ExecutionContext)
  extends Controller
    with ControllerUtils {

  def showOrCreateForForm(id: ApplicationFormId) = Action.async {
    applications.getOrCreateForForm(id).map {
      case Some(app) => Redirect(controllers.routes.ApplicationController.show(app.id))
      case None => NotFound
    }
  }

  def show(id: ApplicationId) = Action.async {
    val t = for {
      a <- OptionT(applications.overview(id))
      af <- OptionT(applicationForms.byId(a.applicationFormId))
      opp <- OptionT(opportunities.byId(af.opportunityId))
    } yield (af, a, opp)

    t.value.map {
      case Some((form, overview, opp)) => Ok(views.html.showApplicationForm(form, overview, opp))
      case None => NotFound
    }
  }

  import ApplicationData._

  def showSectionForm(id: ApplicationId, sectionNumber: Int) = Action.async { request =>
    fieldsFor(sectionNumber) match {
      case Some(fields) =>
        applications.getSection(id, sectionNumber).flatMap { section =>
          val doValidation = request.flash.get("doValidation").exists(_ => true)

          val errs: FieldErrors = section.map { s =>
            if (doValidation) validate(s.answers, rulesFor(sectionNumber)) else noErrors
          }.getOrElse(noErrors)

          renderSectionForm(id, sectionNumber, section, questionsFor(sectionNumber), fields, errs)
        }
      case None => Future.successful(Ok(views.html.wip(routes.ApplicationController.show(id).url)))
    }
  }

  def renderSectionForm(id: ApplicationId, sectionNumber: Int, section: Option[ApplicationSection], questions: Map[String, String], fields: Seq[Field], errs: FieldErrors) = {
    val ft = for {
      a <- OptionT(applications.byId(id))
      af <- OptionT(applicationForms.byId(a.applicationFormId))
      o <- OptionT(opportunities.byId(af.opportunityId))
    } yield (a, af, o)

    ft.value.map {
      case Some((app, appForm, opp)) =>
        val formSection: ApplicationFormSection = appForm.sections.find(_.sectionNumber == sectionNumber).get
        val populatedFields = fields.map {
          _.withValuesFrom(section.map(_.answers).getOrElse(JsObject(Seq())))
            .withErrorsFrom(errs)
            .withQuestionsFrom(questions)
        }

        Ok(views.html.sectionForm(app, section, formSection, opp, populatedFields))
      case None => NotFound
    }
  }

  /**
    * Note if more than one button action name is present in the keys then it is indeterminate as to
    * which one will be returned. This shouldn't occur if the form is properly submitted from a
    * browser, though.
    */
  def decodeButton(keys: Set[String]): Option[ButtonAction] = keys.flatMap(ButtonAction.unapply).headOption.map {
    case Save => if (keys.contains("_complete_checkbox")) Complete else Save
    case b => b
  }

  def postSection(id: ApplicationId, sectionNumber: Int) = Action.async(parse.urlFormEncoded) { implicit request =>
    // Drop keys that start with '_' as these are "system" keys like the button name
    val jsonFormValues = formToJson(request.body.filterKeys(k => !k.startsWith("_")))
    val button: Option[ButtonAction] = decodeButton(request.body.keySet)
    val answers: JsObject = fieldsFor(sectionNumber).map(fs => JsObject(fs.flatMap(_.derender(jsonFormValues)))).getOrElse(JsObject(Seq()))
    val fieldValues = fieldsFor(sectionNumber).map(_.map(_.derender(jsonFormValues))).map(vs => JsObject(vs.flatten)).getOrElse(JsObject(Seq()))

    takeAction(id, sectionNumber, button, fieldValues)
  }

  def takeAction(id: ApplicationId, sectionNumber: Int, button: Option[ButtonAction], fieldValues: JsObject): Future[Result] = {
    button.map {
      case Complete =>
        val rules = rulesFor(sectionNumber)
        val errs = validate(fieldValues, rules)
        if (errs.keySet.isEmpty) {
          applications.completeSection(id, sectionNumber, fieldValues).map { _ =>
            Redirect(routes.ApplicationController.show(id))
          }
        } else {
          applications.saveSection(id, sectionNumber, fieldValues).map { _ =>
            Redirect(routes.ApplicationController.showSectionForm(id, sectionNumber)).flashing(("doValidation", "true"))
          }
        }
      case Save =>
        applications.saveSection(id, sectionNumber, fieldValues).map { _ =>
          Redirect(routes.ApplicationController.show(id))
        }
      case Preview =>
        applications.saveSection(id, sectionNumber, fieldValues).map { _ =>
          Redirect(routes.ApplicationPreviewController.previewSection(id, sectionNumber))
        }
    }.getOrElse(Future.successful(BadRequest))
  }

  def validate(fieldValues: JsObject, rules: Map[String, Seq[FieldRule]]): Map[String, NonEmptyList[String]] = {
    rules.map { case (fieldName, rs) =>
      fieldName -> (fieldValues \ fieldName match {
        case JsDefined(jv) =>
          NonEmptyList.fromList(rs.flatMap(r => r.validate(jv)).toList)
        case _ => if (rs.contains(MandatoryRule)) NonEmptyList.fromList(MandatoryRule.validate(JsString("")).toList) else None
      })
    }.collect { case (fieldName, Some(errs)) => fieldName -> errs }
  }
}
