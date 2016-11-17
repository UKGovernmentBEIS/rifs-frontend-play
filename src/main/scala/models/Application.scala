package models

import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.JsObject

case class ApplicationId(id: Long) extends AnyVal

case class ApplicationSectionId(id: Long) extends AnyVal

case class Application(id: ApplicationId, applicationFormId: ApplicationFormId)

case class ApplicationSection(id: ApplicationSectionId, applicationId: ApplicationId, sectionNumber: Int, answers: JsObject, completedAt: Option[LocalDateTime]) {
  val dtf = DateTimeFormat.forPattern("d MMMM HH:mm")

  def completedAtText: Option[String] = completedAt.map(d => s"Completed ${dtf.print(d)}")

  def isComplete: Boolean = completedAt.isDefined
}

case class ApplicationSectionOverview(sectionNumber: Int, completedAt: Option[LocalDateTime], answers: JsObject) {
  val dtf = DateTimeFormat.forPattern("d MMMM HH:mm")

  def completedAtText: Option[String] = completedAt.map(d => s"Completed ${dtf.print(d)}")

  val status = completedAt.map(_ => "Completed").getOrElse("In progress")
}

case class ApplicationOverview(id: ApplicationId, applicationFormId: ApplicationFormId, sections: Seq[ApplicationSectionOverview])