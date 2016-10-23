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
    } yield (af, a)

    t.value.map {
      case Some((form, overview)) => Ok(views.html.showApplicationForm(form, overview))
      case None => NotFound
    }
  }

  val rules: Map[String, Seq[FieldRule]] = Map("title" -> Seq(WordCountRule(20), MandatoryRule))

  type FieldErrors = Map[String, NonEmptyList[String]]
  val noErrors: FieldErrors = Map()
  val titleFormFields: Seq[Field] = Seq(TextField("What is your event called?", "title", rules.getOrElse("title", Seq()), None, None))

  def showSectionForm(id: ApplicationId, sectionNumber: Int) = Action.async { request =>
    if (sectionNumber == 1) applications.getSection(id, sectionNumber).flatMap { section =>
      val doValidation = request.flash.get("doValidation").exists(_ => true)

      val errs: FieldErrors = section.map { s =>
        if (doValidation) validate(s.answers, rules) else noErrors
      }.getOrElse(noErrors)

      title(id, section, errs)
    }
    else Future.successful(Ok(views.html.wip(routes.ApplicationController.show(id).url)))
  }


  def title(id: ApplicationId, section: Option[ApplicationSection], errs: FieldErrors) = {
    val ft = for {
      a <- OptionT(applications.byId(id))
      af <- OptionT(applicationForms.byId(a.applicationFormId))
      o <- OptionT(opportunities.byId(af.opportunityId))
    } yield (a, af, o)

    ft.value.map {
      case Some((app, appForm, opp)) =>
        val formSection: ApplicationFormSection = appForm.sections.find(_.sectionNumber == 1).get
        val populatedFields = titleFormFields.map {
          _.withValuesFrom(section.map(_.answers).getOrElse(JsObject(Seq())))
            .withErrorsFrom(errs)
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
  def decodeButton(keys: Set[String]): Option[ButtonAction] = keys.flatMap(ButtonAction.unapply).headOption

  def postSection(id: ApplicationId, sectionNumber: Int) = Action.async(parse.urlFormEncoded) { implicit request =>
    // Drop keys that start with '_' as these are "system" keys like the button name
    val jsonFormValues = formToJson(request.body.filterKeys(k => !k.startsWith("_")))
    val button: Option[ButtonAction] = decodeButton(request.body.keySet)

    takeAction(id, sectionNumber, button, JsObject(titleFormFields.flatMap(_.deRender(jsonFormValues))))
  }

  def takeAction(id: ApplicationId, sectionNumber: Int, button: Option[ButtonAction], fieldValues: JsObject): Future[Result] = {
    button.map {
      case Complete =>
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
      case Preview => Future.successful(Redirect(routes.ApplicationController.show(id)))
    }.getOrElse(Future.successful(BadRequest))
  }

  def validate(fieldValues: JsObject, rules: Map[String, Seq[FieldRule]]): Map[String, NonEmptyList[String]] = {
    rules.map { case (fieldName, rs) =>
      fieldName -> (fieldValues \ fieldName match {
        case JsDefined(JsString(s)) => NonEmptyList.fromList(rs.flatMap(r => r.validate(s)).toList)
        case _ => if (rs.contains(MandatoryRule)) NonEmptyList.fromList(MandatoryRule.validate("").toList) else None
      })
    }.collect { case (fieldName, Some(errs)) => fieldName -> errs }
  }
}
