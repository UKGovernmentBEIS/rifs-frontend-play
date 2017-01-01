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

import eu.timepit.refined.auto._
import eu.timepit.refined.numeric._
import org.joda.time.{DateTime, LocalDate}
import org.scalatest.{Matchers, OptionValues, WordSpecLike}

class OpportunityTest extends WordSpecLike with Matchers with OptionValues {
  val testDateBeforeCurrent = LocalDate.now.minusMonths(1)
  val testDateAfterCurrent = LocalDate.now.plusMonths(1)
  val testDateCurrent = LocalDate.now
  val publishedDate: DateTime = DateTime.now.minusMonths(1)

  def opp(startDate: LocalDate, endDate: Option[LocalDate], publishedAt: Option[DateTime]) = Opportunity(OpportunityId(1L), "Test opportunity", startDate, endDate, OpportunityValue(2000.00, "spondulix"), publishedAt, None, Seq())

  "validate" when {
    "Opportunity is published and starts before current date " should {
      "show as status Open" in {
        val o: Opportunity = opp(testDateBeforeCurrent, None, Some(publishedDate))
        o.statusString shouldBe "Open"
      }
    }

    "Opportunity is published and starts on current date " should {
      "show as status Open" in {
        val o: Opportunity = opp(testDateCurrent, None, Some(publishedDate))
        o.statusString shouldBe "Open"
      }
    }

    "Opportunity is published and starts before current date and ends before end date" should {
      "show as status Open" in {
        val o: Opportunity = opp(testDateBeforeCurrent, Some(testDateAfterCurrent), Some(publishedDate))
        o.statusString shouldBe "Open"
      }
    }

    "Opportunity is published and starts after current date" should {
      "show as status Queued" in {
        val o: Opportunity = opp(testDateAfterCurrent, None, Some(publishedDate))
        o.statusString shouldBe "Queued"
      }
    }

    "Opportunity is published and ends before current date" should {
      "show as status Closed" in {
        val o: Opportunity = opp(testDateBeforeCurrent, Some(testDateBeforeCurrent), Some(publishedDate))
        o.statusString shouldBe "Closed"
      }
    }

    "Opportunity is not published" should {
      "show as status Draft" in {
        val o: Opportunity = opp(testDateBeforeCurrent, Some(testDateAfterCurrent), None)
        o.statusString shouldBe "Draft"
      }
    }
  }
}

