package services

import play.api.Logger
import play.api.data.validation.ValidationError
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import services.RestService.{JsonParseException, RestFailure}

import scala.concurrent.{ExecutionContext, Future}

trait RestService {

  def ws: WSClient

  implicit def ec: ExecutionContext

  def getOpt[A: Reads](url: String): Future[Option[A]] = {
    val request: WSRequest = ws.url(url)
    request.get.map { response =>
      response.status match {
        case 200 => response.json.validate[A] match {
          case JsSuccess(a, _) => Some(a)
          case JsError(errs) => throw JsonParseException("GET", request, response, errs)
        }
        case 404 => None
        case _ => throw RestFailure("GET", request, response)
      }
    }
  }

  def getMany[A: Reads](url: String): Future[Seq[A]] = {
    val request: WSRequest = ws.url(url)
    request.get.map { response =>
      response.status match {
        case 200 => response.json.validate[Seq[A]] match {
          case JsSuccess(as, _) => as
          case JsError(errs) => throw JsonParseException("GET", request, response, errs)
        }
        case _ => throw RestFailure("GET", request, response)
      }
    }
  }

  def post[A: Writes](url: String, body: A): Future[Unit] = {
    val request = ws.url(url)
    request.post(Json.toJson(body)).map(_ => ())
  }

  def put[A: Writes](url: String, body: A): Future[Unit] = {
    val request = ws.url(url)
    request.put(Json.toJson(body)).map(_ => ())
  }

  def delete(url: String): Future[Unit] = {
    val request = ws.url(url)
    request.delete().map(_ => ())
  }

  def postWithResult[A: Reads, B: Writes](url: String, body: B): Future[Option[A]] = {
    val request:WSRequest = ws.url(url)
    request.post(Json.toJson(body)).map { response =>
      response.status match {
        case 200 => response.json.validate[A] match {
          case JsSuccess(a, _) =>  Some(a)
          case JsError(errs) => throw JsonParseException("POST", request, response, errs)
        }
        case 404 => None
        case _ => throw RestFailure("POST", request, response)
      }
    }
  }
}

object RestService {

  case class JsonParseException(method: String, request: WSRequest, response: WSResponse, errs: Seq[(JsPath, Seq[ValidationError])]) extends Exception

  case class RestFailure(method: String, request: WSRequest, response: WSResponse) extends Exception {
    val status = response.status
  }

}
