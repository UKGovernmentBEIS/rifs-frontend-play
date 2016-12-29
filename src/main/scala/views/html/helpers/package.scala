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

package views.html

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
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

  def formatId(id: Long): String = f"RIFS $id%04d"

  def formatId(id: Long Refined Positive): String = formatId(id.value)

}
