package forms

import org.scalatest.{Matchers, WordSpecLike}
import play.api.libs.json.{JsNull, JsObject, JsString}

class DateRuleTest extends WordSpecLike with Matchers {
  "DateRule" should {
    "raise three errors if there are no values supplied" in {
      DateRule.validate(JsNull) shouldBe List(
        "'day' cannot be empty",
        "'month' cannot be empty",
        "'year' cannot be empty"
      )
    }

    "raise errors for fields that aren't numbers" in {
      val o = JsObject(Seq(
        "day" -> JsString("foo"),
        "month" -> JsString("bar"),
        "year" -> JsString("baz")
      ))

      DateRule.validate(o) shouldBe List(
        "'day' must be a number",
        "'month' must be a number",
        "'year' must be a number"
      )
    }

    "raise an error for an invalid date" in {
      val o = JsObject(Seq(
        "day" -> JsString("31"),
        "month" -> JsString("4"),
        "year" -> JsString("2017")
      ))

      DateRule.validate(o) shouldBe List("Must provide a valid date")
    }

    "raise no errors when date is valid" in {
      val o = JsObject(Seq(
        "day" -> JsString("30"),
        "month" -> JsString("4"),
        "year" -> JsString("2017")
      ))

      DateRule.validate(o) shouldBe List()
    }
  }

}
