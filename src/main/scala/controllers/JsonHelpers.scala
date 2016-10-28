package controllers

import play.api.libs.json.{JsObject, JsString}

object JsonHelpers {
  def flatten(name: String, o: JsObject): Map[String, String] = {
    import cats.implicits._
    o.fields.map {
      case (n, jo: JsObject) => flatten(s"$name.$n", jo)
      case (n, JsString(s)) => Map(s"$name.$n" -> s)
      // For the moment any non-string value gets dropped
      case (n, _) => Map[String, String]()
    }.fold(Map[String, String]())(_ combine _)
  }
}
