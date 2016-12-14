package views.html

import forms.{TextAreaField, TextField}
import models.ApplicationForm
import play.twirl.api.Html

package object helpers {
  def textForQuestions(appForm: ApplicationForm): Html = {
    val constraints = appForm.sections.flatMap { s =>
      s.questions.flatMap { q =>
        s.fields.find(_.name == q.key).flatMap {
          case TextField(_, _, _, wordCount) => Some(s"Word count: $wordCount")
          case TextAreaField(_, _, wordCount) => Some(s"Word count: $wordCount")
          case _ => None
        }.map(constraintText => (q.key, constraintText))
      }
    }
    val descriptions = appForm.sections.flatMap { s =>
      s.questions.flatMap { q =>
        q.description.map(q.key -> _)
      }
    }

    views.html.partials.oppQuestionsSection(appForm, Map(constraints: _*), Map(descriptions: _*))
  }

}
