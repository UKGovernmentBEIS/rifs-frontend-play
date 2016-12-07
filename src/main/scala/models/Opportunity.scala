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

  val OPPORTUNITY_STATUS_OPEN = "Open"
  val OPPORTUNITY_STATUS_CLOSED = "Closed"
  val OPPORTUNITY_STATUS_DRAFT = "Draft"
  val OPPORTUNITY_STATUS_QUEUED = "Queued"

  lazy val summary: OpportunitySummary = OpportunitySummary(id, title, startDate, endDate, value, publishedAt, duplicatedFrom)

  lazy val statusString: String = {
    publishedAt.isDefined match {
      case true if startDate.isAfter(LocalDate.now()) => OPPORTUNITY_STATUS_QUEUED
      case true if endDate.getOrElse(LocalDate.now()).isBefore(LocalDate.now()) => OPPORTUNITY_STATUS_CLOSED
      case true => OPPORTUNITY_STATUS_OPEN
      case false => OPPORTUNITY_STATUS_DRAFT
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