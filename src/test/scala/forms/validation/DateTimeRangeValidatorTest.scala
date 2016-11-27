package forms.validation

import cats.data.Validated.{Invalid, Valid}
import forms.DateValues
import org.joda.time.LocalDate
import org.scalatest.{Matchers, OptionValues, WordSpecLike}

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
  }
}
