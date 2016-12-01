package models

import org.joda.time.LocalDate

object OpportunityDefs {
  val ABOUT_SECTION_NO = 1
  val QUESTIONS_SECTION_NO = 2
  val CRITERIA_SECTION_NO = 3
}

case class OpportunityId(id: Long) extends AnyVal

case class OpportunityDescriptionSection(sectionNumber: Int, title: String, text: Option[String])

case class OpportunityValue(amount: BigDecimal, unit: String)

case class OpportunityDuration(duration: Int, units: String)

case class Opportunity(
                        id: OpportunityId,
                        title: String,
                        startDate: LocalDate,
                        endDate: Option[LocalDate],
                        value: OpportunityValue,
                        description: Seq[OpportunityDescriptionSection]
                      ) {
  lazy val summary: OpportunitySummary = OpportunitySummary(id, title, startDate, endDate, value)
}

case class OpportunitySummary(
                               id: OpportunityId,
                               title: String,
                               startDate: LocalDate,
                               endDate: Option[LocalDate],
                               value: OpportunityValue
                             )