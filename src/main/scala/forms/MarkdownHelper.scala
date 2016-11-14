package forms

import laika.api._
import laika.parse.markdown.Markdown
import laika.render.HTML
import play.twirl.api.Html

import scala.language.postfixOps


object MarkdownHelper {
  def mdToHtml(s: String): Html = Html(Transform from Markdown to HTML fromString s toString)
}
