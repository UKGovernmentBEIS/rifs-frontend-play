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

import enumeratum.EnumEntry.Lowercase
import enumeratum._
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.refineV
import org.joda.time.{DateTime, LocalDate}

sealed trait OppSectionType extends EnumEntry with Lowercase

object OppSectionType extends Enum[OppSectionType] with PlayJsonEnum[OppSectionType] {
  override def values = findValues

  case object Questions extends OppSectionType

  case object Text extends OppSectionType

}

case class OpportunityId(id: LongId)

object OpportunityId {
  implicit val ordering = new Ordering[OpportunityId] {
    override def compare(x: OpportunityId, y: OpportunityId) = implicitly[Ordering[LongId]].compare(x.id, y.id)
  }
}

case class SectionId(id: LongId)

case class OppSectionNumber(num: PosInt) {
  def next: Option[OppSectionNumber] = refineV[Positive](num + 1).fold(
    _ => None,
    v => Some(OppSectionNumber(v))
  )

  def prev: Option[OppSectionNumber] = refineV[Positive](num - 1).fold(
    _ => None,
    v => Some(OppSectionNumber(v))
  )
}

object OppSectionNumber {
  implicit val ord = new Ordering[OppSectionNumber] {
    override def compare(x: OppSectionNumber, y: OppSectionNumber): Int =
      implicitly[Ordering[PosInt]].compare(x.num, y.num)
  }
}

case class OpportunityDescriptionSection(sectionNumber: OppSectionNumber, title: NonEmptyString, text: Option[NonEmptyString], description: NonEmptyString, helpText: Option[NonEmptyString], sectionType: OppSectionType)

case class OpportunityValue(amount: BigDecimal, unit: String)

case class OpportunityDuration(duration: Int, units: String)

case class Opportunity(
                        id: OpportunityId,
                        title: String,
                        startDate: LocalDate,
                        endDate: Option[LocalDate],
                        value: OpportunityValue,
                        publishedAt: Option[DateTime],
                        duplicatedFrom: Option[OpportunityId],
                        description: Seq[OpportunityDescriptionSection]
                      ) {

  lazy val summary: OpportunitySummary = OpportunitySummary(id, title, startDate, endDate, value, publishedAt, duplicatedFrom)

  lazy val statusString: String = {
    publishedAt.isDefined match {
      case true if isEndDatePassed => "Closed"
      case true if isStartDatePassed => "Open"
      case true => "Queued"
      case false => "Draft"
    }
  }

  val isPublished = publishedAt.isDefined

  lazy val isStartDatePassed = startDate.isEqual(LocalDate.now) || startDate.isBefore(LocalDate.now)

  lazy val isEndDatePassed = endDate.exists(_.isBefore(LocalDate.now))
}

case class OpportunitySummary(
                               id: OpportunityId,
                               title: String,
                               startDate: LocalDate,
                               endDate: Option[LocalDate],
                               value: OpportunityValue,
                               publishedAt: Option[DateTime],
                               duplicatedFrom: Option[OpportunityId]
                             )