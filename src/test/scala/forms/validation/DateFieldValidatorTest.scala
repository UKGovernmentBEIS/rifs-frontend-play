package forms.validation

import forms.DateValues
import org.scalatest.{Matchers, WordSpecLike}

class DateFieldValidatorTest extends WordSpecLike with Matchers {
  import DateFieldValidator._

  "validate" should {
    "give a single error message about an invalid date when fields are missing" in {
      DateFieldValidator(true).validate("test", DateValues(None, None, None)).map {
        _=> fail(s"No error messages were produced!")
      }.leftMap { errs =>
        errs.tail.length shouldBe 0
        errs.head.path shouldBe "test"
        errs.head.err shouldBe mustProvideAValidDateMsg
      }
    }

    "give a single error message about an invalid date when fields are blank" in {
      DateFieldValidator(true).validate("test", DateValues(Some(""), Some(""), Some(""))).map {
        _=> fail(s"No error messages were produced!")
      }.leftMap { errs =>
        errs.tail.length shouldBe 0
        errs.head.path shouldBe "test"
        errs.head.err shouldBe mustProvideAValidDateMsg
      }
    }
     "give a single error message about an invalid date when fields valid ints but don't form a valid date" in {
      DateFieldValidator(true).validate("test", DateValues(Some("31"), Some("6"), Some("2017"))).map {
        _=> fail(s"No error messages were produced!")
      }.leftMap { errs =>
        errs.tail.length shouldBe 0
        errs.head.path shouldBe "test"
        errs.head.err shouldBe mustProvideAValidDateMsg
      }
    }

    "produce an error when date fields are valid but date is in past" in {
      DateFieldValidator(false).validate("test", DateValues(Some("30"), Some("6"), Some("2016"))).map {
        _=> fail(s"No error messages were produced!")
      }.leftMap { errs =>
        errs.tail.length shouldBe 0
        errs.head.path shouldBe "test"
        errs.head.err shouldBe mustBeTodayOrLaterMsg
      }
    }

    "produce no errors when date fields are valid and any date is allowed" in {
      DateFieldValidator(true).validate("test", DateValues(Some("30"), Some("6"), Some("2016"))).leftMap {
        errs => fail(s"Unexpected errors were produced: $errs")
      }
    }

    "produce no errors when date fields are valid after normalisation and any date is allowed" in {
      DateFieldValidator(true).validate("test", DateValues(Some(" 30 "), Some(" 6"), Some("2016 "))).leftMap {
        errs => fail(s"Unexpected errors were produced: $errs")
      }
    }
  }
}
