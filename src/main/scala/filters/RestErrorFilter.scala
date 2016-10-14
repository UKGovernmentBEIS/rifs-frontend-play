package filters

import javax.inject.Inject

import akka.stream.Materializer
import play.api.Logger
import play.api.mvc.{Filter, RequestHeader, Result}
import services.RestService.{JsonParseException, RestFailure}

import scala.concurrent.{ExecutionContext, Future}

class RestErrorFilter @Inject()(implicit val mat: Materializer, ec: ExecutionContext) extends Filter {

  import play.api.mvc.Results._

  override def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    f(rh).recoverWith {
      case JsonParseException(method, request, response, errs) =>
        Logger.error(s"$method to ${request.url} failed with json parse errors")
        Logger.debug(s"response body is ${response.body}")
        Logger.debug(errs.toString())
        Future.successful(BadGateway(errs.toString()))

      case RestFailure(method, request, response) =>
        Logger.error(s"$method to ${request.url} failed with status ${response.status}")
        Logger.debug(s"response body is ${response.body}")
        Future.successful(BadGateway(request.url))
    }
  }
}
