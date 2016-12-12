package models

import org.joda.time.{DateTime, LocalDate}

object OpportunityDefs {
  val ABOUT_SECTION_NO = 1
  val QUESTIONS_SECTION_NO = 2
  val CRITERIA_SECTION_NO = 3
}

case class OpportunityId(id: Long) extends AnyVal

case class OpportunityDescriptionSection(sectionNumber: Int, title: String, text: Option[String], description: Option[String], helpText: Option[String], sectionType: String)

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