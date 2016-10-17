package services

import com.google.inject.ImplementedBy
import models.{ApplicationForm, ApplicationFormId, OpportunityId}
import play.api.libs.json.JsObject

import scala.concurrent.Future

@ImplementedBy(classOf[ApplicationFormService])
trait ApplicationFormOps {
  def byId(id: ApplicationFormId): Future[Option[ApplicationForm]]

  def byOpportunityId(id: OpportunityId): Future[Option[ApplicationForm]]
}


