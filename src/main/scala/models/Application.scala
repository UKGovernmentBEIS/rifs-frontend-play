package models

case class ApplicationId(id: Long) extends AnyVal

case class ApplicationSection(sectionNumber: Int, title: String, started: Boolean)

case class Application(id: ApplicationId, opportunityId: OpportunityId, sections: Seq[ApplicationSection])
