package forms

import play.api.libs.json.{JsString, JsValue}

trait FieldRule {
  def validate(value: JsValue): Seq[String]

  def helpText(value: JsValue): Option[String]
}

case class WordCountRule(maxWords: Int) extends FieldRule {
  def normalise(s: String): String = s.trim()

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

case object MandatoryRule extends FieldRule {
  def normalise(s: String): String = s.trim()

  override def validate(value: JsValue): Seq[String] = {
    val s = value.validate[JsString].asOpt.map(_.value).getOrElse("")

    if (normalise(s) != "") Seq()
    else Seq("Field cannot be empty")
  }

  override def helpText(value: JsValue): Option[String] = None
}