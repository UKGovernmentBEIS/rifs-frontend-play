/*
 * Copyright (C) 2016  Department for Business, Energy and Industrial Strategy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package models

import eu.timepit.refined._
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Positive
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.JsObject

case class ApplicationId(id: LongId)

case class ApplicationSectionId(id: LongId)

case class AppSectionNumber(num: PosInt) {
  def next: Option[AppSectionNumber] = refineV[Positive](num + 1).fold(
    _ => None,
    v => Some(AppSectionNumber(v))
  )

  def prev: Option[AppSectionNumber] = refineV[Positive](num - 1).fold(
    _ => None,
    v => Some(AppSectionNumber(v))
  )
}

object AppSectionNumber {
  implicit val ord = new Ordering[AppSectionNumber] {
    override def compare(x: AppSectionNumber, y: AppSectionNumber): Int =
      implicitly[Ordering[Int]].compare(x.num.value, y.num.value)
  }
}

case class Application(id: ApplicationId, applicationFormId: ApplicationFormId, personalReference: Option[NonEmptyString])

case class ApplicationDetail(
                              id: ApplicationId,
                              personalReference: Option[NonEmptyString],
                              sectionCount: NonNegativeInt,
                              completedSectionCount: NonNegativeInt,
                              opportunity: OpportunitySummary,
                              applicationForm: ApplicationForm,
                              sections: Seq[ApplicationSection]) {
  def sectionDetail(sectionNumber: AppSectionNumber): ApplicationSectionDetail =
    ApplicationSectionDetail(
      id,
      sectionCount,
      completedSectionCount,
      opportunity,
      // TODO: remove the naked get
      applicationForm.sections.find(_.sectionNumber == sectionNumber).get,
      sections.find(_.sectionNumber == sectionNumber)
    )

  def section(num: AppSectionNumber): Option[ApplicationSection] = sections.find(_.sectionNumber == num)
}

case class ApplicationSectionDetail(
                                     id: ApplicationId,
                                     sectionCount: NonNegativeInt,
                                     completedSectionCount: NonNegativeInt,
                                     opportunity: OpportunitySummary,
                                     formSection: ApplicationFormSection,
                                     section: Option[ApplicationSection]
                                   ) {
  val sectionNumber = formSection.sectionNumber
}

case class SubmittedApplicationRef(applicationRef: Long) extends AnyVal

case class ApplicationSection(sectionNumber: AppSectionNumber, answers: JsObject, completedAt: Option[LocalDateTime]) {
  def isComplete: Boolean = completedAt.isDefined

  val dtf = DateTimeFormat.forPattern("d MMMM YYYY h:mma")

  val completedAtText: Option[String] =
    completedAt.map(d => s"Completed ${dtf.print(d)}".replaceAll("PM$", "pm").replaceAll("AM$", "am"))

  val status = completedAt.map(_ => "Completed").getOrElse("In progress")
}

case class ApplicationSectionOverview(sectionNumber: AppSectionNumber, completedAt: Option[LocalDateTime], answers: JsObject) {
  def isComplete: Boolean = completedAt.isDefined

  val dtf = DateTimeFormat.forPattern("d MMMM YYYY h:mma")

  val completedAtText: Option[String] =
    completedAt.map(d => s"Completed ${dtf.print(d)}".replaceAll("PM$", "pm").replaceAll("AM$", "am"))

  val status = completedAt.map(_ => "Completed").getOrElse("In progress")
}

case class ApplicationOverview(id: ApplicationId, applicationFormId: ApplicationFormId, sections: Seq[ApplicationSectionOverview])