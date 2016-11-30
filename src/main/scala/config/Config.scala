package config

case class BusinessConfig(baseUrl: String)

case class Config(logAssets: Option[Boolean], logRequests: Boolean, business: BusinessConfig)

object Config {

  import pureconfig._

  lazy val config: Config = loadConfig[Config].get
}