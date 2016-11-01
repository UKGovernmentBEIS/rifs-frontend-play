package controllers

import forms._
import forms.validation._
import models.Question
import play.api.libs.json._

object ApplicationData {

  import FieldChecks._

  implicit val dvReads = Json.reads[DateValues]
  implicit val dwdReads = Json.reads[DateWithDaysValues]

  private val provisionalDateValidator: DateWithDaysValidator = DateWithDaysValidator(allowPast = false, 1, 9)

  def checksFor(sectionNumber: Int): Map[String, FieldCheck] = {
    sectionNumber match {
      case 1 => Map("title" -> mandatoryText(20))
      case 2 => Map("provisionalDate" -> fromValidator(provisionalDateValidator))
      case 3 => Map("eventObjectives" -> mandatoryText(500))
      case 4 => Map("topicAndSpeaker" -> mandatoryText(500))
      case 5 => Map("eventAudience" -> mandatoryText(500))
      case _ => Map()
    }
  }

  def previewChecksFor(sectionNumber: Int): Map[String, FieldCheck] = {
    sectionNumber match {
      case 1 => Map("title" -> mandatoryCheck)
      case 2 => Map("provisionalDate" -> fromValidator(provisionalDateValidator))
      case 3 => Map("eventObjectives" -> mandatoryCheck)
      case 4 => Map("topicAndSpeaker" -> mandatoryCheck)
      case 5 => Map("eventAudience" -> mandatoryCheck)
      case _ => Map()
    }
  }

  val eventObjHelp = "There are no fixed rules about content; however the most successful events have involved senior academics working with " +
    "colleagues to develop the research programme and share their strategic vision.\nFeedback from previous events has shown that it is important to keep the demands on time modest, with most seminars scheduled over a half day."

  val eventObjDescriptionVal = "Explain what outcomes you hope the event will achieve, including who is likely to benefit and the actions you'll take to maximise the benefits."
  val eventObjFormQuestions = Map("eventObjectives" -> Question("What are the objectives of the event?", Some(eventObjDescriptionVal), Some(eventObjHelp)))
  val eventObjFormFields: Seq[Field] = Seq(TextAreaField(None, "eventObjectives"))

  val topicAndSpeakerHelp = "Possible topics for discussion include intellectual asset management, licensing and collaborative R&D.\n" +
    "Speakers might include internal or external business development professionals and others such as patent lawyers/agents and KTP advisors.\n" +
    "Whenever possible, a member of our Swindon office staff will be available to participate in the seminar free of charge."
  val topicAndSpeakerDescVal = "List the subjects and speakers you are planning for the event. It doesn't matter if they are not confirmed at this stage."
  val topicAndSpeakerQuestions = Map("topicAndSpeaker" -> Question("What topics do you intend to cover?", Some(topicAndSpeakerDescVal), Some(topicAndSpeakerHelp)))
  val topicAndSpeakerFields: Seq[Field] = Seq(TextAreaField(None, "topicAndSpeaker"))

  val eventAudienceHelp = "If possible, examine the audience make-up from previous similar events. Who came to them and who is likely to come to your event?\n" +
    "It's a good idea to invite people from relevant faculties, colleges or departments, and business development offices."
  val eventAudienceDescVal = "There may be one or more target audiences. How many people do you expect to attend? What sectors (for example, academic, industrial, legal) will they represent?"
  val eventAudienceQuestions = Map("eventAudience" -> Question("Who is the event's target audience?", Some(eventAudienceDescVal), Some(eventAudienceHelp)))
  val eventAudienceFields: Seq[Field] = Seq(TextAreaField(None, "eventAudience"))


  val titleFormQuestions = Map("title" -> Question("What is your event called?"))
  val dateFormQuestions = Map(
    "provisionalDate.days" -> Question("How long will it last?"),
    "provisionalDate.date" -> Question("When do you propose to hold the event?")
  )

  def questionsFor(sectionNumber: Int): Map[String, Question] = {
    sectionNumber match {
      case 1 => titleFormQuestions
      case 2 => dateFormQuestions
      case 3 => eventObjFormQuestions
      case 4 => topicAndSpeakerQuestions
      case 5 => eventAudienceQuestions
      case _ => Map()
    }
  }

  val titleFormFields: Seq[Field] = Seq(TextField(None, "title", isNumeric = false))
  val dateFormFields: Seq[Field] = Seq(DateWithDaysField("provisionalDate", provisionalDateValidator))


  def fieldsFor(sectionNum: Int): Option[Seq[Field]] = {
    sectionNum match {
      case 1 => Some(titleFormFields)
      case 2 => Some(dateFormFields)
      case 3 => Some(eventObjFormFields)
      case 4 => Some(topicAndSpeakerFields)
      case 5 => Some(eventAudienceFields)
      case _ => None
    }
  }
}
