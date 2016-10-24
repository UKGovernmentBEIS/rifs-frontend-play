package forms

import cats.syntax.validated._
import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import play.api.libs.json.JsString

class WordCountRuleTest extends WordSpecLike with Matchers with OptionValues {
  val sut = WordCountRule(2)
  "WordCountRule.validate" should {
    "be valid for an empty string" in {
      sut.validate(JsString("")) shouldBe Seq()
    }

    "normalise whitespace to an empty string" in {
      sut.validate(JsString("  \t\n")) shouldBe Seq()
    }

    "be invalid when word count is exceeded" in {
      sut.validate(JsString("one two three")).size shouldBe 1
    }
  }

  "WordCountRule.helpText" should {
    "return correct text for empty string" in {
      sut.helpText(JsString("")).value shouldBe "2 words maximum"
    }

    "return correct text for string of whitespace" in {
      sut.helpText(JsString(" \t\n")).value shouldBe "2 words maximum"
    }

    "return correct text for string of 1 word" in {
      sut.helpText(JsString("foo")).value shouldBe "Words remaining: 1"
    }

    "return correct text for string of 2 words" in {
      sut.helpText(JsString("foo bar ")).value shouldBe "Words remaining: 0"
    }

    "return correct text for string of 3 words" in {
      sut.helpText(JsString("foo bar baz")).value shouldBe "1 word over limit"
    }

    "return correct text for string of 4 words" in {
      sut.helpText(JsString("foo bar baz boo")).value shouldBe "2 words over limit"
    }
  }
}
