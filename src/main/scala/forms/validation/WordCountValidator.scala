package forms.validation

import cats.data.ValidatedNel
import cats.syntax.validated._

case class WordCountValidator(maxWords: Int) extends FieldValidator[String, String] {

  import WordCountValidator._

  override def normalise(s: String): String = s.trim()

  override def validate(s: String): ValidatedNel[String, String] = {
    normalise(s) match {
      case n if n.split("\\s+").length <= maxWords => "Word limit exceeded".invalidNel
      case n => n.validNel
    }
  }

  override def hintText(s: String): Option[String] = {
    val wordCount = normalise(s).split("\\s+").length

    val text = wordCount match {
      case 0 => noWords(maxWords)
      case _ if wordCount > maxWords => overLimit(wordCount - maxWords)
      case _ => wordsRemaining(maxWords - wordCount)
    }

    Some(text)
  }
}

object WordCountValidator {
  def ws(n: Int) = if (n == 1) "word" else "words"

  def wordsRemaining(wordsLeft: Int) = s"Words remaining: $wordsLeft"

  def overLimit(over: Int) = s"$over ${ws(over)} over limit"

  def noWords(max: Int) = s"$max ${ws(max)} maximum"
}