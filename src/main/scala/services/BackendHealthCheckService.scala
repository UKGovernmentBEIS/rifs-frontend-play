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

package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import config.Config
import play.api.libs.json.JsObject
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[BackendHealthCheckService])
trait BackendHealthCheckOps {
  def version() : Future[JsObject]
}

class BackendHealthCheckService @Inject()(val ws: WSClient)(implicit val ec: ExecutionContext) extends BackendHealthCheckOps with  RestService {
  val baseUrl = Config.config.business.baseUrl

  override def version() : Future[JsObject] = {
    val url = s"$baseUrl/version"
    getOpt[JsObject](url).map(_.getOrElse(JsObject(Seq())))
  }

}
