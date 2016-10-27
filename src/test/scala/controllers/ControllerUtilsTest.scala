package controllers

import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import play.api.libs.json.{JsArray, JsDefined, JsString}

import scala.collection.mutable.ArrayBuffer

class ControllerUtilsTest extends WordSpecLike with Matchers with OptionValues{
  val sut = new ControllerUtils {}

  "formToJson" should {

    "convert a field with a single value to a JsString" in {
      val input : Map[String, Seq[String]] = Map("foo" -> ArrayBuffer("bar"))

      val o = sut.formToJson(input)

      o.value.get("foo").value shouldBe JsString("bar")
    }

    "convert a field with a two values to a JsArray" in {
      val input : Map[String, Seq[String]] = Map("foo" -> ArrayBuffer("bar", "baz"))

      val o = sut.formToJson(input)

      o.value.get("foo").value shouldBe JsArray(Seq(JsString("bar"), JsString("baz")))
    }

  }
}
