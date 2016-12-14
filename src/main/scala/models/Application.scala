package models

import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.JsObject

case class ApplicationId(id: Long) extends AnyVal

case class ApplicationSectionId(id: Long) extends AnyVal

case class Application(id: ApplicationId, applicationFormId: ApplicationFormId, personalReference: Option[String])

case class ApplicationDetail(
                              id: ApplicationId,
                              personalReference: Option[String],
                              sectionCount: Int,
                              completedSectionCount: Int,
                              opportunity: OpportunitySummary,
                              applicationForm: ApplicationForm,
                              sections: Seq[ApplicationSection]) {
  def sectionDetail(sectionNumber: Int): ApplicationSectionDetail =
    ApplicationSectionDetail(
      id,
      sectionCount,
      completedSectionCount,
      opportunity,
      // TODO: remove the naked get
      applicationForm.sections.find(_.sectionNumber == sectionNumber).get,
      sections.find(_.sectionNumber == sectionNumber)
    )
}

case class ApplicationSectionDetail(
                                     id: ApplicationId,
                                     sectionCount: Int,
                                     completedSectionCount: Int,
                                     opportunity: OpportunitySummary,
                                     formSection: ApplicationFormSection,
                                     section: Option[ApplicationSection]
                                   ){
  val sectionNumber = formSection.sectionNumber
}

case class SubmittedApplicationRef(applicationRef: Long) extends AnyVal

case class ApplicationSection(sectionNumber: Int, answers: JsObject, completedAt: Option[LocalDateTime]) {
  def isComplete: Boolean = completedAt.isDefined

  val dtf = DateTimeFormat.forPattern("d MMMM YYYY h:mma")

  val completedAtText: Option[String] =
    completedAt.map(d => s"Completed ${dtf.print(d)}".replaceAll("PM$", "pm").replaceAll("AM$", "am"))

  val status = completedAt.map(_ => "Completed").getOrElse("In progress")
}

case class ApplicationSectionOverview(sectionNumber: Int, completedAt: Option[LocalDateTime], answers: JsObject) {
  def isComplete: Boolean = completedAt.isDefined

  val dtf = DateTimeFormat.forPattern("d MMMM YYYY h:mma")

  val completedAtText: Option[String] =
    completedAt.map(d => s"Completed ${dtf.print(d)}".replaceAll("PM$", "pm").replaceAll("AM$", "am"))

  val status = completedAt.map(_ => "Completed").getOrElse("In progress")
}

case class ApplicationOverview(id: ApplicationId, applicationFormId: ApplicationFormId, sections: Seq[ApplicationSectionOverview])