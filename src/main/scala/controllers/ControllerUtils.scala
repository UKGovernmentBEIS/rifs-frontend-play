package controllers

import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}

trait ControllerUtils {
  def formToJson(form: Map[String, Seq[String]]): JsObject = {
    val jmap: Map[String, JsValue] = form.map { case (k, v) => k -> v.toList }.map {
      case (k, s :: Nil) => k -> JsString(s)
      case (k, ss) => k -> JsArray(ss.map(JsString))
    }

    JsObject(jmap)
  }
}
