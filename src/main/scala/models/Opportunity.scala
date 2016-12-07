package models

import org.joda.time.{DateTime, LocalDate}

object OpportunityDefs {
  val ABOUT_SECTION_NO = 1
  val QUESTIONS_SECTION_NO = 2
  val CRITERIA_SECTION_NO = 3
}

case class OpportunityId(id: Long) extends AnyVal

case class OpportunityDescriptionSection(sectionNumber: Int, title: String, text: Option[String], description: Option[String], helpText: Option[String])

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
      case true if startDate.isAfter(LocalDate.now()) => "Queued"
      case true if endDate.isEmpty => "Open"
      case true if endDate.get.isBefore(LocalDate.now()) => "Closed"
      case true => "Open"
      case false => "Draft"
    }
  }
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