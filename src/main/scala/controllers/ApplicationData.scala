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

  val eventObjFormQuestions = Map("eventObjectives" -> "What are the objectives of the event??")
  val eventObjDescriptionVal = "Exaplain what outcomes you hope the event will achieve, Including who is likely to benefit and the actions you'll take to maximise te benefits"
  val eventObjDescription = Map("description" -> eventObjDescriptionVal)
  val eventObjHelp = "There are no fixed rules about content; however the most successful events have involved senior academics working with " +
     "colleagues to develop the research programme and share their strategic vision. \n\n\n\n\nFeedback from previous events has shown that it is important to keep the demands on time modest, with most seminars scheduled over a half day."

  //val eventObjFormFields: Seq[Field] = Seq(TextAreaField(None, "eventObjectives", titleFormRules.getOrElse("title", Seq())))
  val eventObjFormFields: Seq[Field] = Seq(TextAreaField(None, "eventObjectives", Seq() ))
  val eventObjFormRules: Map[String, Seq[FieldRule]] = Map("eventObjectives" -> Seq(WordCountRule(500), MandatoryRule))


  def rulesFor(sectionNumber: Int) :Map[String, Seq[FieldRule]] = {
    sectionNumber match {
      case 1 => titleFormRules
      case 2 => dateFormRules
      case 3 => eventObjFormRules
      case _ => Map()
    }
  }

  def questionsFor(sectionNumber: Int): Map[String, String] = {
    sectionNumber match {
      case 1 => titleFormQuestions
      case 2 => dateFormQuestions
      case 3 => eventObjFormQuestions
      case _ => Map()
    }
  }

  def fieldsFor(sectionNum: Int): Option[Seq[Field]] = {
    sectionNum match {
      case 1 => Some(titleFormFields)
      case 2 => Some(dateFormFields)
      case 3 => Some(eventObjFormFields)
      case _ => None
    }
  }
}
