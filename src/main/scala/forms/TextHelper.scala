package forms

object TextHelper {

  sealed trait Split

  case class Text(s: String) extends Split

  case object Break extends Split

  case class Paragraph(s: String) extends Split

  val multiBreak = "(\\r\\n|\\n){2,}"

  /**
    * Break the string up based on line-break characters. Single line breaks translate
    * into a Break and 2 or more consecutive line breaks translate to a Paragraph
    */
  def splitLines(s: String): List[Split] = {
    s.split(multiBreak).toList.map(Paragraph(_))
  }

}
