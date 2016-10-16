package models

import play.api.libs.json.JsObject

case class ApplicationId(id: Long) extends AnyVal

case class ApplicationSection(sectionNumber: Int, title: String, started: Boolean, doc: JsObject)

case class Application(id: ApplicationId, opportunityId: OpportunityId, sections: Seq[ApplicationSection])
