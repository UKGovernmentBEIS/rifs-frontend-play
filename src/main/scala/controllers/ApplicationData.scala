package controllers

import cats.data.NonEmptyList
import forms._

object ApplicationData {
  val titleFormRules: Map[String, Seq[FieldRule]] = Map("title" -> Seq(WordCountRule(20), MandatoryRule))
  val dateFormRules: Map[String, Seq[FieldRule]] = Map(
    "date" -> Seq(DateRule(allowPast = false)),
    "days"-> Seq(MandatoryRule, IntRule(1,9)))

  type FieldErrors = Map[String, NonEmptyList[String]]
  val noErrors: FieldErrors = Map()
  val titleFormFields: Seq[Field] = Seq(TextField(None, "title", titleFormRules.getOrElse("title", Seq())))
  val titleFormQuestions = Map("title" -> "What is your event called?")

  val dateFormQuestions = Map(
    "days" -> "How long will it last?",
    "date" -> "When do you propose to hold the event?"
  )

  val dateFormFields: Seq[Field] = Seq(
    DateField("date", Seq()),
    TextField(Some("Day(s)"), "days", Seq())
  )

  def rulesFor(sectionNumber: Int) :Map[String, Seq[FieldRule]] = {
    sectionNumber match {
      case 1 => titleFormRules
      case 2 => dateFormRules
      case _ => Map()
    }
  }

  def questionsFor(sectionNumber: Int): Map[String, String] = {
    sectionNumber match {
      case 1 => titleFormQuestions
      case 2 => dateFormQuestions
      case _ => Map()
    }
  }

  def fieldsFor(sectionNum: Int): Option[Seq[Field]] = {
    sectionNum match {
      case 1 => Some(titleFormFields)
      case 2 => Some(dateFormFields)
      case _ => None
    }
  }
}
