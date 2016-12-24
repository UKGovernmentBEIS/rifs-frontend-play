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

package controllers

import play.api.libs.json._

object JsonHelpers {

  def flatten(o: JsObject): Map[String, String] = flatten("", o)

  def flatten(name: String, o: JsObject): Map[String, String] = {
    def subName(n: String) = if (name == "") n else s"$name.$n"

    import cats.implicits._
    o.fields.map {
      case (n, jo: JsObject) => flatten(subName(n), jo)
      case (n, JsString(s)) => Map(subName(n) -> s)
      case (n, JsNumber(num)) => Map(subName(n) -> num.toString)
      // HACK: for the moment treat an array as a big string
      case (n, JsArray(values)) => Map(subName(n) -> (if (values.isEmpty) "" else values.mkString))
      // For the moment any non-string value gets dropped
      case (n, _) => Map[String, String]()
    }.fold(Map[String, String]())(_ combine _)
  }

  def unflatten(values: Map[String, String]): JsObject = {
    deflate(values.map { case (k, v) => k.split('.').toList -> List(v) })
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

  def allFieldsEmpty(doc: JsObject): Boolean = flatten(doc).forall { case (_, v) => v.trim() == "" }
}
