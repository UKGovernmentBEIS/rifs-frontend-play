package controllers

import play.api.libs.json.{JsArray, JsObject, JsString}

object JsonHelpers {
  def flatten(name: String, o: JsObject): Map[String, String] = {

    def subName(n: String) = if (name == "") n else s"$name.$n"

    import cats.implicits._
    o.fields.map {
      case (n, jo: JsObject) => flatten(subName(n), jo)
      case (n, JsString(s)) => Map(subName(n) -> s)
      // For the moment any non-string value gets dropped
      case (n, _) => Map[String, String]()
    }.fold(Map[String, String]())(_ combine _)
  }

  def formToJson(form: Map[String, Seq[String]]): JsObject = {
    deflate(form.map { case (k, vs) => k.split('.').toList -> vs.toList })
  }

  def deflate(form: Map[List[String], List[String]]): JsObject = {
    val os: List[JsObject] = form.toList.map {
      case (k :: Nil, s :: Nil) => JsObject(Seq(k -> JsString(s)))
      case (k :: Nil, ss) => JsObject(Seq(k -> JsArray(ss.map(JsString))))
      case (k :: ks, ss) => JsObject(Seq(k -> deflate(Map(ks -> ss))))
      case _ => JsObject(Seq())
    }

    os.fold(JsObject(Seq()))(_.deepMerge(_))
  }
}
