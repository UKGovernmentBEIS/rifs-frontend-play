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

import cats.data.Validated.{Invalid, Valid}
import forms.DateValues
import org.joda.time.LocalDate
import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import play.api.libs.json.Json

class DateTimeRangeValidatorTest extends WordSpecLike with Matchers with OptionValues {
  val validDateValues1 = Some(DateValues(Some("31"), Some("1"), Some("2017")))
  val validDateValues2 = Some(DateValues(Some("31"), Some("3"), Some("2017")))
  val path = "dates"

  "validate" should {
    val validator = DateTimeRangeValidator(allowPast = true, isEndDateMandatory = true)

    "succeed if both start and end " + path + " are supplied and are valid" in {
      val values = DateTimeRangeValues(validDateValues1, validDateValues2, None)
      validator.validate(path, values) shouldBe a[Valid[_]]
    }

    "fail with a single error if the start date is not provided" in {
      val values = DateTimeRangeValues(None, validDateValues2, None)
      val result = validator.validate(path, values)
      result shouldBe a[Invalid[_]]
      result.leftMap { errs =>
        errs.tail.length shouldBe 0
        errs.head.path shouldBe s"$path.startDate"
      }
    }

    "fail with a single error if the end date is before the start date" in {
      val values = DateTimeRangeValues(validDateValues2, validDateValues1, None)
      val result = validator.validate(path, values)
      result shouldBe a[Invalid[_]]
      result.leftMap { errs =>
        errs.tail.length shouldBe 0
        errs.head.path shouldBe path
      }
    }

    "fail if the start date is before today" in {
      val today = new LocalDate()
      val yesterdayValues = DateValues(Some((today.getDayOfMonth - 1).toString), Some(today.getMonthOfYear.toString), Some(today.getYear.toString))

      val values = DateTimeRangeValues(Some(yesterdayValues), None, None)
      val result = DateTimeRangeValidator(allowPast = false, isEndDateMandatory = false).validate(path, values)
      result shouldBe an[Invalid[_]]
      result.leftMap { errs =>
        errs.tail.length shouldBe 0
        errs.head.path shouldBe s"$path.startDate"
      }
    }

    "fail with one error if endDateProvided is 'yes' but end date fields are blank strings" in {
      val values = DateTimeRangeValues(validDateValues2, Some(DateValues(Some(""), Some(""), Some(""))), Some("yes"))
      val result = validator.validate(path, values)
      result shouldBe a[Invalid[_]]
      result.leftMap { errs =>
        errs.tail.length shouldBe 0
        errs.head.path shouldBe s"$path.endDate"
        errs.head.err shouldBe validator.mustProvideValidEndDateMessage
      }
    }
  }
}
