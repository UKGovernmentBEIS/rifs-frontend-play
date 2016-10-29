package models

case class Question(text: String, longDescription: Option[String] = None, helpText: Option[String] = None)
