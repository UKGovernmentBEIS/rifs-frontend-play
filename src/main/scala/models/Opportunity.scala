package models

import enumeratum.EnumEntry.Lowercase
import enumeratum._
import org.joda.time.{DateTime, LocalDate}

sealed trait OppSectionType extends EnumEntry with Lowercase

object OppSectionType extends Enum[OppSectionType] with PlayJsonEnum[OppSectionType] {
  override def values = findValues

  case object Questions extends OppSectionType

  case object Text extends OppSectionType

}


case class OpportunityId(id: Long) extends AnyVal

case class OpportunityDescriptionSection(sectionNumber: Int, title: String, text: Option[String], description: Option[String], helpText: Option[String], sectionType: OppSectionType)

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