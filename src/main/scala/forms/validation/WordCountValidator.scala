/*
 * Copyright (C) 2016  Department for Business, Energy and Industrial Strategy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package forms.validation

import cats.data.ValidatedNel
import cats.syntax.validated._
import forms.validation.FieldValidator.Normalised

case class WordCountValidator(maxWords: Int) extends FieldValidator[String, String] {

  import WordCountValidator._

  override def normalise(s: String): String = s.trim()

  override def doValidation(path: String, s: Normalised[String]): ValidatedNel[FieldError, String] = {
    s match {
      case n if n.split("\\s+").length > maxWords => FieldError(path, "Word limit exceeded").invalidNel
      case n => n.validNel
    }
  }

  override def doHinting(path: String, s: Normalised[String]): List[FieldHint] = {
    val wordCount = s.split("\\s+").filterNot(_ == "").length

    val text = wordCount match {
      case 0 => noWords(maxWords)
      case _ if wordCount > maxWords => overLimit(wordCount - maxWords)
      case _ => wordsRemaining(maxWords - wordCount)
    }

    List(FieldHint(path, text, Some("WordCount"), Some(s"""{\"maxWords\": $maxWords}""")))
  }
}

object WordCountValidator {
  def ws(n: Int) = if (n == 1) "word" else "words"

  def wordsRemaining(wordsLeft: Int) = s"Words remaining: $wordsLeft"

  def overLimit(over: Int) = s"$over ${ws(over)} over limit"

  def noWords(max: Int) = s"$max ${ws(max)} maximum"
}