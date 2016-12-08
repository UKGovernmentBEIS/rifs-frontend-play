package controllers

import javax.inject.Inject

import play.api.libs.json.{JsObject, JsString, Json}
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

    val backendVersion = backend.version().map { backendVersion =>
      JsObject(Seq("rifs-business" -> backendVersion))
    }.recover {
      case t => JsObject(Seq("rifs-business" -> JsString(s"Could not retrieve version: ${t.getMessage}")))
    }

    backendVersion.map { bv =>
      Ok(ourVersion + ("services" -> bv))
    }
  }
}
