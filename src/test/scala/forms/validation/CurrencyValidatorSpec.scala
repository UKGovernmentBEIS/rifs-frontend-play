package forms.validation

import cats.data.Validated.Invalid
import cats.syntax.validated._
import org.scalatest.{Matchers, WordSpecLike}

class CurrencyValidatorSpec extends WordSpecLike with Matchers {
  "currency validator" should {
    "reject a non-numeric value" in {
      CurrencyValidator.validate("", "foo") shouldBe an[Invalid[_]]
    }

    "accept a numeric value and round it to 2dp" in {
      CurrencyValidator.validate("","123.456") shouldBe BigDecimal(123.46).valid
    }

    "accept a numeric value containing commas" in {
      CurrencyValidator.validate("","1,123.456") shouldBe BigDecimal(1123.46).valid
    }

    "accept a numeric value with leading and trailing whitespace" in {
      CurrencyValidator.validate("","\t 123.456\n\r") shouldBe BigDecimal(123.46).valid
    }
  }
}
