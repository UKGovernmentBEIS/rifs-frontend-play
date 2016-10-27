package controllers

import cats.data.NonEmptyList
import forms._
import models.Question

object ApplicationData {
  val titleFormRules: Map[String, Seq[FieldRule]] = Map("title" -> Seq(WordCountRule(20), MandatoryRule()))
  val dateFormRules: Map[String, Seq[FieldRule]] = Map(
    "date" -> Seq(DateRule(allowPast = false)),
    "days" -> Seq(MandatoryRule(), IntRule(1, 9)))

  type FieldErrors = Map[String, NonEmptyList[String]]
  val noErrors: FieldErrors = Map()
  val titleFormFields: Seq[Field] = Seq(TextField(None, "title", titleFormRules.getOrElse("title", Seq())))
  val titleFormQuestions = Map("title" -> Question("What is your event called?"))

  val dateFormQuestions = Map(
    "days" -> Question("How long will it last?"),
    "date" -> Question("When do you propose to hold the event?")
  )

  val dateFormFields: Seq[Field] = Seq(
    DateField("date", Seq()),
    TextField(Some("Day(s)"), "days", Seq())
  )

  val eventObjHelp = "There are no fixed rules about content; however the most successful events have involved senior academics working with " +
    "colleagues to develop the research programme and share their strategic vision. \n\n\n\n\nFeedback from previous events has shown that it is important to keep the demands on time modest, with most seminars scheduled over a half day."

  val eventObjDescriptionVal = "Explain what outcomes you hope the event will achieve, Including who is likely to benefit and the actions you'll take to maximise te benefits"
  val eventObjFormQuestions = Map("eventObjectives" -> Question("What are the objectives of the event?", Some(eventObjDescriptionVal), Some(eventObjHelp)))
  val eventObjFormRules: Map[String, Seq[FieldRule]] = Map("eventObjectives" -> Seq(WordCountRule(500), MandatoryRule()))
  val eventObjFormFields: Seq[Field] = Seq(TextAreaField(None, "eventObjectives", eventObjFormRules.getOrElse("eventObjectives", Seq())))


  val topicAndSpeakerHelp = "Possible topics for discussion include intellectual asset management, licensing and collaborative " +
    "R&D.\n\n\n\n\nSpeakers might include internal or external business development professionals and others " +
    "such as patent lawyers/agents and KTP advisors.\n\n\n\n\nWhenever possible, a member of our Swindon office " +
    "staff will be available to participate in the seminar free of charge."
  val topicAndSpeakerDescVal = "List the subjects and speakers you are planning for the event. It doesn’t matter if they are not confirmed at this stage."
  val topicAndSpeakerQuestions = Map("topicAndSpeaker" -> Question("What topics do you intend to cover?", Some(topicAndSpeakerDescVal), Some(topicAndSpeakerHelp)))
  val topicAndSpeakerRules: Map[String, Seq[FieldRule]] = Map("topicAndSpeaker" -> Seq(WordCountRule(500), MandatoryRule()))
  val topicAndSpeakerFields: Seq[Field] = Seq(TextAreaField(None, "topicAndSpeaker", topicAndSpeakerRules.getOrElse("topicAndSpeaker", Seq())))

  def rulesFor(sectionNumber: Int): Map[String, Seq[FieldRule]] = {

    sectionNumber match {
      case 1 => titleFormRules
      case 2 => dateFormRules
      case 3 => eventObjFormRules
      case 4 => topicAndSpeakerRules
      case _ => Map()
    }
  }

  def questionsFor(sectionNumber: Int): Map[String, Question] = {
    sectionNumber match {
      case 1 => titleFormQuestions
      case 2 => dateFormQuestions
      case 3 => eventObjFormQuestions
      case 4 => topicAndSpeakerQuestions
      case _ => Map()
    }
  }

  def fieldsFor(sectionNum: Int): Option[Seq[Field]] = {
    sectionNum match {
      case 1 => Some(titleFormFields)
      case 2 => Some(dateFormFields)
      case 3 => Some(eventObjFormFields)
      case 4 => Some(topicAndSpeakerFields)
      case _ => None
    }
  }
}
