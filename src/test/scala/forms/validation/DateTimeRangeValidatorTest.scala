package forms.validation

import cats.data.Validated.{Invalid, Valid}
import forms.DateValues
import org.scalatest.{Matchers, OptionValues, WordSpecLike}

class DateTimeRangeValidatorTest extends WordSpecLike with Matchers with OptionValues {
  val validDateValues1 = Some(DateValues(Some("31"), Some("1"), Some("2017")))
  val validDateValues2 = Some(DateValues(Some("31"), Some("3"), Some("2017")))

  "validate" should {
    val validator = DateTimeRangeValidator(allowPast = true, isEndDateMandatory = true)
    "succeed if both start and end dates are supplied and are valid" in {
      val values = DateTimeRangeValues(validDateValues1, validDateValues2, endDateProvided = true)

      validator.validate("", values) shouldBe a[Valid[_]]
    }

    "fail if the start date is not provided" in {
      val values = DateTimeRangeValues(None, validDateValues2, endDateProvided = true)

      validator.validate("", values) shouldBe a[Invalid[_]]
    }

    "fail if the end date is before the start date" in {
      val values = DateTimeRangeValues(validDateValues2, validDateValues1, endDateProvided = true)

      validator.validate("", values) shouldBe a[Invalid[_]]
    }
  }

}
