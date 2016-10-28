package forms

import cats.data.NonEmptyList
import models.Question
import play.api.libs.json._
import play.twirl.api.Html

trait Field {
  def renderFormInput: Html

  def renderPreview: Html

  def name: String

  def rules: Seq[FieldRule]

  def errs: Option[NonEmptyList[String]]

  /**
    * Provide a hook by which the renderer can look at the fields from the html form and
    * extract a value for the answer to the application question. Most of the time there is
    * a one-to-one correspondence between the html field and answer (handled by the default
    * implementation provided her), but in some cases, say a date where the Day/Month/Year
    * are presented as separate form fields, the renderer might want to collect those fields
    * into a single value.
    *
    * @param fieldValues a JsObject containing the fields submitted from the HTML form
    * @return either a mapping of the field name to a value (as a Some) or None if a value
    *         could not be de-rendered
    */
  def derender(fieldValues: JsObject): Seq[(String, JsValue)] = fieldValues \ name match {
    case JsDefined(v) => Seq(name -> v)
    case _ => Seq()
  }

  def withValuesFrom(values: JsObject): Field

  def withErrorsFrom(errs: Map[String, NonEmptyList[String]]): Field

  def withQuestionsFrom(questions: Map[String, Question]): Field

  protected def stringValue(o: JsObject, n: String): Option[String] = (o \ n).validate[JsString].asOpt.map(_.value)

  protected def objectValue(o: JsObject, n: String): Option[JsObject] = (o \ n).validate[JsObject].asOpt
}

