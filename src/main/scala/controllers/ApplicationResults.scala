package controllers

import models.ApplicationId
import play.api.mvc.Results._

trait ApplicationResults {

  def wip(backLink: String) = Redirect(controllers.routes.OpportunityController.wip(backLink))

  def sectionFormCall(applicationId: ApplicationId, sectionNumber: Int) =
    controllers.routes.ApplicationController.showSectionForm(applicationId, sectionNumber)

  def redirectToSectionForm(applicationId: ApplicationId, sectionNumber: Int) = Redirect(sectionFormCall(applicationId, sectionNumber))

  def redirectToOverview(applicationId: ApplicationId) = Redirect(controllers.routes.ApplicationController.show(applicationId))

}
