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

package models

import forms.Field
import play.api.libs.json._

case class ApplicationFormId(id: Long) extends AnyVal

case class ApplicationFormQuestion(key: String, text: String, description: Option[String], helpText: Option[String])

case class ApplicationFormSection(sectionNumber: Int, title: String, questions: Seq[ApplicationFormQuestion], sectionType: ApplicationFormSectionType, fields: Seq[Field]) {
  /**
    * Convenience function to turn the sequence of `ApplicationFormQuestions` sent by the backend into a
    * Map of `String -> Question` used by the form templates
    *
    * @return
    */
  lazy val questionMap: Map[String, Question] = {
    questions
      .groupBy(_.key)
      .mapValues(_.headOption)
      .collect { case (k, Some(q)) => k -> q }
      .mapValues { q =>
        Question(q.text, q.description, q.helpText)
      }
  }
}

case class ApplicationForm(id: ApplicationFormId, opportunityId: OpportunityId, sections: Seq[ApplicationFormSection])

sealed trait ApplicationFormSectionType {
  def name: String
}

object ApplicationFormSectionType {
  def apply(s: String): Option[ApplicationFormSectionType] =
    s.trim.toLowerCase match {
      case SectionTypeForm.name => Some(SectionTypeForm)
      case SectionTypeList.name => Some(SectionTypeList)
      case _ => None
    }

  implicit val appSecTypeReads = new Reads[ApplicationFormSectionType] {
    override def reads(json: JsValue): JsResult[ApplicationFormSectionType] =
      json.validate[JsString].flatMap { s =>
        ApplicationFormSectionType(s.value) match {
          case Some(t) => JsSuccess(t)
          case None => JsError(s"could not convert string value '$s' to an ApplicationFormSectionType")
        }
      }
  }
}

case object SectionTypeForm extends ApplicationFormSectionType {
  override val name: String = "form"
}

case object SectionTypeList extends ApplicationFormSectionType {
  override val name: String = "list"
}
