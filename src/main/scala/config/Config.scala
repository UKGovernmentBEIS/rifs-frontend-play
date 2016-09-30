package config

case class Config(logRequests: Boolean)

object Config {

  import pureconfig._

  lazy val config: Config = loadConfig[Config].get
}