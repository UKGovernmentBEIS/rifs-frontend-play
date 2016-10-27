package forms

import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import play.api.libs.json.{JsObject, Json}

class DateWithDaysSpec extends WordSpecLike with Matchers with OptionValues {
  "derender" should {
    "extract from field values" in {
      val testData = Json.parse("""{"date__day":"2","date__month":"3","date__year":"2107","days":"3"}""").as[JsObject]

      val result = DateWithDaysField("x", DateField("date"), TextField(None, "days")).derender(testData)

      val expected = ("x", Json.parse("""{"date":{"day":"2","month":"3","year":"2107"},"days":"3"}"""))
      result.value shouldBe expected
    }
  }

}
