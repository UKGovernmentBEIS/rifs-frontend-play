package forms.validation

import org.scalatest.{Matchers, OptionValues, WordSpecLike}

class DateWithDaysValidatorSpec extends WordSpecLike with Matchers with OptionValues {
  val validator = DateWithDaysValidator(allowPast = false, 1, 9)
  "validate" when {
    "all fields are blank" should {
      val result = validator.validate("test", DateWithDaysValues(None, None)).swap.toOption.value.toList
      "return four errors" in {
        result.length shouldBe 4
      }
      "contain an error for the day field" in {
        result.find(_.path == "test.date.day").value.err shouldBe "'day' cannot be empty"
      }
      "contain an error for the month field" in {
        result.find(_.path == "test.date.month").value.err shouldBe "'month' cannot be empty"
      }
      "contain an error for the year field" in {
        result.find(_.path == "test.date.year").value.err shouldBe "'year' cannot be empty"
      }
      "contain an error for the days field" in {
        result.find(_.path == "test.days").value.err shouldBe "Must be a whole number"
      }
    }
  }


}
