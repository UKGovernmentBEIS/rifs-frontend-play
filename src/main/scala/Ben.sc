
import play.api.libs.json._

import scala.concurrent.Future
import scala.util.parsing.json.JSON._
import controllers.JsonHelpers


val ob: JsObject = JsObject(Seq(
  "date" -> JsObject(Seq(
    "day" -> JsString("1"),
    "year" -> JsString(""),
    "month" -> JsString("3")
  )),
  "days" ->  JsString("4")
))

println(ob.keys)

val answers : Map[String, String] =  JsonHelpers.flatten("date", ob)
JsonHelpers.flatten("", ob).find(_._2 != "")
answers.size
answers.find(_._2 != "")

answers.filter(_._2 != "").toList
println ("DDDDD")
//answers.toList.sortBy()
JsonHelpers.flatten("", ob).filter(_._2 != "").toList.sortBy(_._2).length

