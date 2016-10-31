package forms

import forms.validation.{FieldError, FieldHint}
import models.Question
import play.twirl.api.Html

trait Field {
  def renderFormInput(questions: Map[String, Question], answers: Map[String, String], errs: Seq[FieldError], hints: Seq[FieldHint]): Html

  def renderPreview(answers: Map[String, String]): Html

  def name: String
}

