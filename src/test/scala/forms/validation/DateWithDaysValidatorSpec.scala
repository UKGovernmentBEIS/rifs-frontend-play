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
      val now = LocalDate.now
      val dateValues = DateValues(Some(now.getDayOfMonth.toString), Some((now.getMonthOfYear - 1).toString), Some(now.getYear.toString))
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
    val now = LocalDate.now
    val dateValues = DateValues(Some(now.getDayOfMonth.toString), Some((now.getMonthOfYear - 1).toString), Some(now.getYear.toString))
    val result = validator.validate("test", DateWithDaysValues(Some(dateValues), Some("2")))

    "Return no error" in {
      result shouldBe a[Valid[_]]
    }
  }
}
