package controllers

import forms._
import forms.validation._
import play.api.Logger
import play.api.libs.json._

object ApplicationData {

  type FieldErrors = List[FieldError]
  val noErrors: FieldErrors = List()
  type FieldHints = List[FieldHint]

  trait FieldCheck {
    def apply(path: String, value: JsValue): List[FieldError]

    def hint(path: String, value: JsValue): Option[FieldHint]
  }

  def fromValidator[T: Reads](v: FieldValidator[T, _]): FieldCheck = new FieldCheck {
    override def apply(path: String, jv: JsValue) = jv.validate[T].map { x =>
      v.validate(path, x).fold(_.toList, _ => List())
    } match {
      case JsSuccess(msgs, _) => msgs
      case JsError(errs) => List(FieldError(path, "Could not decode form values!"))
    }

    override def hint(path: String, jv: JsValue): Option[FieldHint] = v.hintText(path, jv.validate[String].asOpt)
  }

  def decodeString(jv: JsValue): Option[String] = {
    jv match {
      case JsString(s) => Some(s)
      case _ => None
    }
  }

  val titleValidator: FieldValidator[Option[String], String] = MandatoryValidator.andThen(WordCountValidator(20))

  val titleCheck: FieldCheck = new FieldCheck {
    override def apply(path: String, jv: JsValue) = titleValidator.validate(path, decodeString(jv)).fold(_.toList, _ => List())

    override def hint(path: String, value: JsValue): Option[FieldHint] = {
      Logger.debug(s"hinting $path with value $value")
      titleValidator.hintText(path, value.validate[String].asOpt)
    }
  }

  val titleFormChecks: Map[String, FieldCheck] = Map("title" -> titleCheck)

  implicit val dvReads = Json.reads[DateValues]
  implicit val dwdReads = Json.reads[DateWithDaysValues]
  val daysCheck: FieldCheck = new FieldCheck {
    val validator: FieldValidator[Option[String], Int] = MandatoryValidator.andThen(IntValidator(1, 9))

    override def apply(path: String, jv: JsValue) = validator.validate(path, decodeString(jv)).fold(_.toList, _ => List())

    override def hint(path: String, value: JsValue): Option[FieldHint] = validator.hintText(path, value.validate[String].asOpt)
  }

  val dateFormChecks: Map[String, FieldCheck] = Map {
    "provisionalDate" -> fromValidator(DateWithDaysValidator(allowPast = false, 1, 9))
  }

  def checksFor(sectionNumber: Int): Map[String, FieldCheck] = {
    sectionNumber match {
      case 1 => titleFormChecks
      case 2 => dateFormChecks
      case _ => Map()
    }
  }

  def previewChecksFor(sectionNumber: Int): Map[String, FieldCheck] = {
    sectionNumber match {
      case 1 => titleFormChecks
      case 2 => dateFormChecks
      case _ => Map()
    }
  }

  val titleFormQuestions = Map("title" -> "What is your event called?")
  val dateFormQuestions = Map(
    "provisionalDate.days" -> "How long will it last?",
    "provisionalDate.date" -> "When do you propose to hold the event?"
  )

  def questionsFor(sectionNumber: Int): Map[String, String] = {
    sectionNumber match {
      case 1 => titleFormQuestions
      case 2 => dateFormQuestions
      case _ => Map()
    }
  }

  val titleFormFields: Seq[Field] = Seq(TextField(None, "title"))
  val dateFormFields: Seq[Field] = Seq(DateWithDaysField("provisionalDate", DateField("provisionalDate.date"), TextField(None, "provisionalDate.days")))

  def fieldsFor(sectionNum: Int): Option[Seq[Field]] = {
    sectionNum match {
      case 1 => Some(titleFormFields)
      case 2 => Some(dateFormFields)
      case _ => None
    }
  }
}
