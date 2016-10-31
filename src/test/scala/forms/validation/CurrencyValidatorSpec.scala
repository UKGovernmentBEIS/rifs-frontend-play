package forms.validation

import cats.data.Validated.Invalid
import cats.syntax.validated._
import org.scalatest.{Matchers, WordSpecLike}

class CurrencyValidatorSpec extends WordSpecLike with Matchers {
  "currency validator" should {
    "reject a missing value" in {
      CurrencyValidator.validate("", None) shouldBe an[Invalid[_]]
    }

    "reject a non-numeric value" in {
      CurrencyValidator.validate("", Some("foo")) shouldBe an[Invalid[_]]
    }

    "accept a numeric value and round it to 2dp" in {
      CurrencyValidator.validate("", Some("123.456")) shouldBe BigDecimal(123.46).valid
    }

    "accept a numeric value containing commas" in {
      CurrencyValidator.validate("", Some("1,123.456")) shouldBe BigDecimal(1123.46).valid
    }

    "accept a numeric value with leading and trailing whitespace" in {
      CurrencyValidator.validate("", Some("\t 123.456\n\r")) shouldBe BigDecimal(123.46).valid
    }
  }
}
