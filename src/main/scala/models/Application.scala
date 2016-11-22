package models

import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.JsObject

case class ApplicationId(id: Long) extends AnyVal

case class ApplicationSectionId(id: Long) extends AnyVal

case class Application(id: ApplicationId, applicationFormId: ApplicationFormId)

case class ApplicationDetail(
                              id: ApplicationId,
                              sectionCount: Int,
                              completedSectionCount: Int,
                              opportunity: OpportunitySummary,
                              applicationForm: ApplicationForm,
                              sections: Seq[ApplicationSection])

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