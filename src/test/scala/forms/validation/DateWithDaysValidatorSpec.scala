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

package forms.validation

import cats.data.Validated.Valid
import forms.DateValues
import org.joda.time.LocalDate
import org.scalatest.{Matchers, OptionValues, WordSpecLike}

class DateWithDaysValidatorSpec extends WordSpecLike with Matchers with OptionValues {

  "validate" when {
    "all fields are blank" should {
      val validator = DateWithDaysValidator(allowPast = false, 1, 9)
      val result = validator.validate("test", DateWithDaysValues(None, None)).swap.toOption.value.toList
      "return two errors" in {
        result.length shouldBe 2
      }
      "contain an error for the date field" in {
        result.find(_.path == "test.date").value.err shouldBe DateFieldValidator.mustProvideAValidDateMsg
      }
      "contain an error for the days field" in {
        result.find(_.path == "test.days").value.err shouldBe "Must be a whole number"
      }
    }

    "date is in the past and `allowPast` is false" should {
      val validator = DateWithDaysValidator(allowPast = false, 1, 9)
      val yesterday = LocalDate.now.minusDays(1)
      val dateValues = DateValues(Some(yesterday.getDayOfMonth.toString), Some(yesterday.getMonthOfYear.toString), Some(yesterday.getYear.toString))
      val result = validator.validate("test", DateWithDaysValues(Some(dateValues), Some("2"))).swap.toOption.value.toList

      "return one error" in {
        result.length shouldBe 1
      }
      "contain an error for the date field" in {
        result.find(_.path == "test.date").value.err shouldBe DateFieldValidator.mustBeTodayOrLaterMsg
      }
    }
  }

  "date is in the past and `allowPast` is true" should {
    val validator = DateWithDaysValidator(allowPast = true, 1, 9)
    val yesterday = LocalDate.now.minusDays(1)
    val dateValues = DateValues(Some(yesterday.getDayOfMonth.toString), Some(yesterday.getMonthOfYear.toString), Some(yesterday.getYear.toString))
    val result = validator.validate("test", DateWithDaysValues(Some(dateValues), Some("2")))

    "Return no error" in {
      result shouldBe a[Valid[_]]
    }
  }
}
