package forms

import play.api.libs.json.{JsString, JsValue}

trait FieldRule {
  def validateOnPreview: Boolean

  def validate(value: JsValue): Seq[String]

  def helpText(value: JsValue): Option[String]

  def configAsJson: Option[String] = None
}

case class WordCountRule(maxWords: Int, validateOnPreview: Boolean = false) extends FieldRule {
  def normalise(s: String): String = s.trim()

  override def configAsJson = Some(s"""{\"maxWords\": $maxWords}""")

  override def validate(value: JsValue): Seq[String] = {
    val s = value.validate[JsString].asOpt.map(_.value).getOrElse("")

    if (normalise(s).split("\\s+").length <= maxWords) Seq()
    else Seq("Word limit exceeded")
  }

  override def helpText(value: JsValue): Option[String] = {
    val s = value.validate[JsString].asOpt.map(_.value).getOrElse("")

    val wordCount = normalise(s).split("\\s+").length

    import WordCountRule._
    val text =
      if (normalise(s) == "") noWordsText(maxWords)
      else if (wordCount > maxWords) overLimitText(wordCount - maxWords)
      else wordsLeft(maxWords - wordCount)

    Some(text)
  }
}

object WordCountRule {
  def w(n: Int) = if (n == 1) "word" else "words"

  def wordsLeft(wordsLeft: Int) = s"Words remaining: $wordsLeft"

  def overLimitText(over: Int) = {
    s"$over ${w(over)} over limit"
  }

  def noWordsText(max: Int) = s"$max ${w(max)} maximum"
}

case class MandatoryRule(validateOnPreview: Boolean = true) extends FieldRule {
  def normalise(s: String): String = s.trim()

  override def validate(value: JsValue): Seq[String] = {
    val s = value.validate[JsString].asOpt.map(_.value).getOrElse("")

    if (normalise(s) != "") Seq()
    else Seq("Field cannot be empty")
  }

  override def helpText(value: JsValue): Option[String] = None
}

case class IntRule(minValue: Int = Int.MinValue, maxValue: Int = Int.MaxValue, validateOnPreview: Boolean = true) extends FieldRule {
  override def validate(value: JsValue): Seq[String] = {
    value.validate[JsString].asOpt.map(_.value).getOrElse("") match {
      case s if s.trim() == "" => Seq() // if field is blank don't do validation - leave it to MandatoryRule
      case ParseInt(i) if i < minValue => Seq(s"Minimum value is $minValue")
      case ParseInt(i) if i > maxValue => Seq(s"Maximum value is $maxValue")
      case ParseInt(i) => Seq()
      case _ => Seq("Must be a whole number")
    }
  }

  override def helpText(value: JsValue): Option[String] = None
}