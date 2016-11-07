package controllers

import forms.validation._
import play.api.Logger
import play.api.libs.json._

trait FieldCheck {
  def apply(path: String, value: JsValue): List[FieldError]

  def hint(path: String, value: JsValue): List[FieldHint]
}

object FieldChecks {
  val mandatoryCheck = new FieldCheck {
    override def apply(path: String, value: JsValue): List[FieldError] =
      MandatoryValidator(None).validate(path, value.validate[String].asOpt).fold(_.toList, _ => List())

    override def hint(path: String, value: JsValue): List[FieldHint] = List()
  }

  def mandatoryText(wordLimit: Int, displayName: Option[String] = None) = new FieldCheck {
    val validator = MandatoryValidator(displayName).andThen(WordCountValidator(wordLimit))

    override def apply(path: String, value: JsValue): List[FieldError] = validator.validate(path, decodeString(value)).fold(_.toList, _ => List())

    override def hint(path: String, value: JsValue): List[FieldHint] = validator.hintText(path, value)
  }

  val currencyValidator = new FieldCheck {
    override def apply(path: String, value: JsValue): List[FieldError] = CurrencyValidator.validate(path, decodeString(value)).fold(_.toList, _ => List())

    override def hint(path: String, value: JsValue): List[FieldHint] = CurrencyValidator.hintText(path, value)
  }

  def fromValidator[T: Reads](v: FieldValidator[T, _]): FieldCheck = new FieldCheck {
    override def toString(): String = s"check from validator $v"

    override def apply(path: String, jv: JsValue) = jv.validate[T].map { x =>
      v.validate(path, x).fold(_.toList, _ => List())
    } match {
      case JsSuccess(msgs, _) => msgs
      case JsError(errs) =>
        Logger.debug(errs.toString)
        List(FieldError(path, "Could not decode form values!"))
    }

    override def hint(path: String, jv: JsValue): List[FieldHint] = {
      Logger.debug(s"hinting value $jv at path $path")
      v.hintText(path, jv)
    }
  }


  def intFieldCheck(min: Int, max: Int, displayName: Option[String] = None) = new FieldCheck {
    val validator: FieldValidator[Option[String], Int] = MandatoryValidator(displayName).andThen(IntValidator(min, max))

    override def apply(path: String, jv: JsValue) = validator.validate(path, decodeString(jv)).fold(_.toList, _ => List())

    override def hint(path: String, value: JsValue): List[FieldHint] = validator.hintText(path, value)
  }

  def decodeString(jv: JsValue): Option[String] = {
    jv match {
      case JsString(s) => Some(s)
      case _ => None
    }
  }
}