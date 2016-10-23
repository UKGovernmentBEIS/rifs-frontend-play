package forms

import cats.data.NonEmptyList
import play.api.libs.json._
import play.twirl.api.Html

trait Field {
  def renderFormInput: Html

  def name: String

  def rules: Seq[FieldRule]

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
  def deRender(fieldValues: JsObject): Option[(String, JsValue)] = fieldValues \ name match {
    case JsDefined(v) => Some(name -> v)
    case _ => None
  }

  def withValuesFrom(values: JsObject): Field

  def withErrorsFrom(errs: Map[String, NonEmptyList[String]]): Field

  protected def stringValue(o: JsObject, n: String): Option[String] = (o \ n).validate[JsString].asOpt.map(_.value)

  protected def objectValue(o: JsObject, n: String): Option[JsObject] = (o \ n).validate[JsObject].asOpt
}

case class TextField(label: String, name: String, rules: Seq[FieldRule], value: Option[String], errs: Option[NonEmptyList[String]]) extends Field {
  override def withValuesFrom(values: JsObject): TextField = this.copy(value = stringValue(values, name))

  override def withErrorsFrom(errs: Map[String, NonEmptyList[String]]): Field = this.copy(errs = errs.get(name))

  override def renderFormInput: Html = views.html.renderers.textField(this)
}

case class DateValues(day: String, month: String, year: String)

case class DateField(label: String, name: String, rules: Seq[FieldRule], value: Option[DateValues], errs: Option[NonEmptyList[String]]) extends Field {
  override def renderFormInput: Html = views.html.renderers.dateField(this)

  override def withValuesFrom(values: JsObject): DateField = {
    val value = objectValue(values, name).map { o =>
      val day = stringValue(o, s"${name}__day").getOrElse("")
      val month = stringValue(o, s"${name}__month").getOrElse("")
      val year = stringValue(o, s"${name}__year").getOrElse("")
      DateValues(day, month, year)
    }
    this.copy(value = value)
  }

  // TODO: Implement this
  override def withErrorsFrom(errs: Map[String, NonEmptyList[String]]): DateField = this
}