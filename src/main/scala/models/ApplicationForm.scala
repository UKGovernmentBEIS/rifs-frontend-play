package models

case class ApplicationFormId(id: Long) extends AnyVal

case class ApplicationFormSection(sectionNumber: Int, title: String)

case class ApplicationForm(id: ApplicationFormId, opportunityId: OpportunityId, sections: Seq[ApplicationFormSection])
