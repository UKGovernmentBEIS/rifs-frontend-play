package models

import org.joda.time.LocalDateTime
import play.api.libs.json.JsObject

case class ApplicationId(id: Long) extends AnyVal

case class ApplicationSectionId(id: Long) extends AnyVal

case class Application(id: ApplicationId, applicationFormId: ApplicationFormId)

case class ApplicationSection(id: ApplicationSectionId, applicationId: ApplicationId, sectionNumber: Int, answers: JsObject, completedAt: Option[LocalDateTime])

case class ApplicationSectionOverview(sectionNumber: Int, status: String)

case class ApplicationOverview(id: ApplicationId, applicationFormId: ApplicationFormId, sections: Seq[ApplicationSectionOverview])