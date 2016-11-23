package forms.validation

import cats.data.Validated.{Invalid, Valid}
import forms.DateValues
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

    "fail with three startDate errors if the start date is not provided" in {
      val values = DateTimeRangeValues(None, validDateValues2, None)
      val result = validator.validate(path, values)
      result shouldBe a[Invalid[_]]
      result.leftMap { errs =>
        errs.tail.length shouldBe 2
        errs.map(_.path should startWith(s"$path.startDate"))
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
  }

}
