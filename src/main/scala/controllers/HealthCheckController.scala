package controllers

import javax.inject.Inject

import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, Controller}
import rifs.frontend.buildinfo.BuildInfo
import services.BackendHealthCheckOps

import scala.concurrent.ExecutionContext

class HealthCheckController @Inject()(backend: BackendHealthCheckOps)(implicit ec: ExecutionContext) extends Controller {
  def ping = Action {
    Ok("alive")
  }

  def version = Action.async { implicit request =>
    // need to convert the Anys to Strings so play json knows how to
    // convert it
    val ourVersion = Json.toJson(BuildInfo.toMap.mapValues(_.toString)).as[JsObject]

    backend.version().map { backendVersion =>
      val serviceVersions = JsObject(Seq("rifs-business" -> backendVersion))
      Ok(ourVersion + ("services" -> serviceVersions))
    }
  }
}
