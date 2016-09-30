package config

case class BusinessConfig(baseUrl: String)

case class Config(logRequests: Boolean, business: BusinessConfig)

object Config {

  import pureconfig._

  lazy val config: Config = loadConfig[Config].get
}