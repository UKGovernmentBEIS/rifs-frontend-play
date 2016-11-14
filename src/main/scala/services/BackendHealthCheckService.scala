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
