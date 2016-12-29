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

import cats.data.Validated.Invalid
import cats.syntax.validated._
import org.scalatest.{Matchers, WordSpecLike}

class CurrencyValidatorSpec extends WordSpecLike with Matchers {
  "currency validator" should {
    "reject a missing value" in {
      CurrencyValidator().validate("", None) shouldBe an[Invalid[_]]
    }

    "reject a non-numeric value" in {
      CurrencyValidator().validate("", Some("foo")) shouldBe an[Invalid[_]]
    }

    "accept a numeric value and round it to 2dp" in {
      CurrencyValidator.anyValue.validate("", Some("123.456")) shouldBe BigDecimal(123.46).valid
    }

    "accept a numeric value containing commas" in {
      CurrencyValidator().validate("", Some("1,123.456")) shouldBe BigDecimal(1123.46).valid
    }

    "accept a numeric value with leading and trailing whitespace" in {
      CurrencyValidator().validate("", Some("\t 123.456\n\r")) shouldBe BigDecimal(123.46).valid
    }

    "Reject 0 or negative" in {
      CurrencyValidator.greaterThanZero.validate("", Some("0") )  shouldBe an[Invalid[_]]
      CurrencyValidator.greaterThanZero.validate("", Some("- 0.01") )  shouldBe an[Invalid[_]]
    }

    "Reject values which are less then minimum specified" in {
      CurrencyValidator.apply(1).validate("", Some("1") )  shouldBe an[Invalid[_]]
    }

    "Accept values that are greater then minimum specified" in {
      CurrencyValidator.apply(1).validate("", Some("1.01") )  shouldBe BigDecimal(1.01).valid
    }
  }
}
