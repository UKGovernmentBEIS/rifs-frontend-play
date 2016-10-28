package forms

import java.text.SimpleDateFormat
import java.util.GregorianCalendar

import cats.data.NonEmptyList
import models.Question
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.twirl.api.Html

case class DurationField(name: String, startDate: DateField, duration: TextField) extends Field {

  override def errs: Option[NonEmptyList[String]] = {
    if (startDate.errs.isEmpty) return duration.errs
    if (duration.errs.isEmpty) return startDate.errs
    return Some(startDate.errs.get++duration.errs.get.toList)
  }

  def formattedStartTime: Option[String] = startDate.value.map(x => new SimpleDateFormat("dd MMMM yyyy").format(new GregorianCalendar(Integer.parseInt(x.year), Integer.parseInt(x.month) -1, Integer.parseInt(x.day)).getTime()))
  def formattedEndTime: Option[String] = (for {
    sd <- startDate.value
    d <- duration.value
  } yield(sd, d)).map(x => new SimpleDateFormat("dd MMMM yyyy").format(new GregorianCalendar(Integer.parseInt(x._1.year), Integer.parseInt(x._1.month) -1, Integer.parseInt(x._1.day) + Integer.parseInt(x._2)).getTime()))


  override def renderFormInput: Html = Html(views.html.renderers.dateField(startDate).toString() + views.html.renderers.textField(duration).toString())

  override def renderPreview: Html = views.html.renderers.preview.durationField(this)

  override def rules: Seq[FieldRule] = startDate.rules ++ duration.rules

  override def derender(fieldValues: JsObject): Seq[(String, JsValue)] = {
    startDate.derender(fieldValues) ++ duration.derender(fieldValues)
  }

  override def withValuesFrom(values: JsObject): DurationField = {
    this.copy(startDate = startDate.withValuesFrom(values), duration = duration.withValuesFrom(values))
  }

  override def withErrorsFrom(errs: Map[String, NonEmptyList[String]]): DurationField = {
    this.copy(startDate = startDate.withErrorsFrom(errs), duration = duration.withErrorsFrom(errs))
  }

  override def withQuestionsFrom(questions: Map[String,   Question]): Field = this.copy(
    startDate = startDate.withQuestionsFrom(questions), duration = duration.withQuestionsFrom(questions)
  )
}