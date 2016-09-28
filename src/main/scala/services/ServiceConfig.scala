package services

import scala.util.{Failure, Success}

case class ServiceConfig(api: ApiConfig, client: ClientConfig)

case class ClientConfig(id: String, secret: String, useSSL: Boolean)

case class ApiConfig(host: String, isSandbox: Option[Boolean] = None, isLocal: Option[Boolean] = None, callbackURL: String) {
  val baseURI =
    host +
      (if (isLocal.getOrElse(false)) "" else "/apprenticeship-levy") +
      (if (isSandbox.getOrElse(false)) "/sandbox" else "")

  val accessTokenUri = s"$host/oauth/token"
  val authorizeSchemeUri = s"$host/oauth/authorize"
}

object ServiceConfig {

  import pureconfig._

  lazy val config = loadConfig[ServiceConfig] match {
    case Success(c) => c
    case Failure(t) => throw t
  }
}
