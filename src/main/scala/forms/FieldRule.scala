package forms

import cats.data.ValidatedNel
import cats.syntax.validated._

trait FieldRule {
  def validate(value: String): ValidatedNel[String, String]

  def helpText(value: String): Option[String]
}

case class WordCountRule(maxWords: Int) extends FieldRule {
  def normalise(s: String): String = s.trim()

  override def validate(value: String): ValidatedNel[String, String] = {
    if (normalise(value).split("\\s+").length <= maxWords) normalise(value).valid
    else "Word limit exceeded".invalidNel
  }

  override def helpText(value: String): Option[String] = {
    val wordCount = normalise(value).split("\\s+").length

    import WordCountRule._
    val text =
      if (normalise(value) == "") noWordsText(maxWords)
      else if (wordCount > maxWords) overLimitText(wordCount - maxWords)
      else wordsLeft(maxWords - wordCount)

    Some(text)
  }
}

object WordCountRule {
  def w(n: Int) = if (n == 1) "word" else "words"

  def wordsLeft(wordsLeft: Int) = s"$wordsLeft ${w(wordsLeft)} left"

  def overLimitText(over: Int) = {
    s"$over ${w(over)} over limit"
  }

  def noWordsText(max: Int) = s"$max ${w(max)} maximum"
}

case object MandatoryRule extends FieldRule {
  def normalise(s: String): String = s.trim()

  override def validate(value: String): ValidatedNel[String, String] = {
    if (normalise(value) != "") normalise(value).valid
    else "Must be supplied".invalidNel
  }

  override def helpText(value: String): Option[String] = None
}