package filters

import javax.inject.Inject

import akka.stream.Materializer
import config.Config
import play.api.Logger
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class LoggingFilter @Inject()(implicit val mat: Materializer, ec: ExecutionContext) extends Filter {
  lazy val logAssets = Config.config.logAssets.getOrElse(false)

  def apply(nextFilter: RequestHeader => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {

    if (requestHeader.uri.startsWith("/assets") && !logAssets) nextFilter(requestHeader)
    else {
      val startTime = System.currentTimeMillis

      Logger.trace(s"${requestHeader.method} ${requestHeader.uri} received...")

      nextFilter(requestHeader).map { result =>

        val endTime = System.currentTimeMillis
        val requestTime = endTime - startTime

        Logger.info(s"${requestHeader.method} ${requestHeader.uri} took ${requestTime}ms and returned ${result.header.status}")

        result.withHeaders("Request-Time" -> requestTime.toString)
      }
    }
  }
}