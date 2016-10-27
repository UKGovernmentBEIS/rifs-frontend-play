package controllers

import cats.data.NonEmptyList
import forms._
import forms.validation.{FieldValidator, MandatoryValidator, WordCountValidator}

object ApplicationData {


  type FieldErrors = Map[String, NonEmptyList[String]]
  val noErrors: FieldErrors = Map()

  type FieldCheck = Option[String] => List[String]

  def fromValidator(v: FieldValidator[Option[String], _]): FieldCheck = os => v.validate(os).fold(_.toList, _ => List())

  val titleValidator: FieldValidator[Option[String], String] = MandatoryValidator.andThen(WordCountValidator(20))

  val titleCheck: FieldCheck = fromValidator(titleValidator)

  val titleFormValidations : Map[String, FieldCheck] = Map("title" -> titleCheck)

  val titleFormRules: Map[String, Seq[FieldRule]] = Map("title" -> Seq(WordCountRule(20), MandatoryRule()))
  val dateFormRules: Map[String, Seq[FieldRule]] = Map(
    "date" -> Seq(DateRule(allowPast = false)),
    "days" -> Seq(MandatoryRule(), IntRule(1, 9)))

  def rulesFor(sectionNumber: Int): Map[String, Seq[FieldRule]] = {
    sectionNumber match {
      case 1 => titleFormRules
      case 2 => dateFormRules
      case _ => Map()
    }
  }

  val titleFormQuestions = Map("title" -> "What is your event called?")
  val dateFormQuestions = Map(
    "days" -> "How long will it last?",
    "date" -> "When do you propose to hold the event?"
  )

  def questionsFor(sectionNumber: Int): Map[String, String] = {
    sectionNumber match {
      case 1 => titleFormQuestions
      case 2 => dateFormQuestions
      case _ => Map()
    }
  }

  val titleFormFields: Seq[Field] = Seq(TextField(None, "title", titleFormRules.getOrElse("title", Seq())))
  val dateFormFields: Seq[Field] = Seq(DateWithDaysField("provisionalDate", DateField("date"), TextField(None, "days")))

  def fieldsFor(sectionNum: Int): Option[Seq[Field]] = {
    sectionNum match {
      case 1 => Some(titleFormFields)
      case 2 => Some(dateFormFields)
      case _ => None
    }
  }
}
