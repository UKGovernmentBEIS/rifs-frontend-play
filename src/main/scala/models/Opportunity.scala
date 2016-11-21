package models

case class OpportunityId(id: Long) extends AnyVal

case class OpportunityDescriptionSection(sectionNumber:Int, title: String, paragraphs: Seq[String])

case class OpportunityValue(amount: BigDecimal, unit: String)

case class OpportunityDuration(duration: Int, units: String)

case class Opportunity(
                        id: OpportunityId,
                        title: String,
                        startDate: String,
                        duration: Option[OpportunityDuration],
                        value: OpportunityValue,
                        description: Seq[OpportunityDescriptionSection]
                      )

case class OpportunitySummary(
                        id: OpportunityId,
                        title: String,
                        startDate: String,
                        duration: Option[OpportunityDuration],
                        value: OpportunityValue
                      )