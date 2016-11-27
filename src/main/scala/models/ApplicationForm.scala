package models

import forms.Field

case class ApplicationFormId(id: Long) extends AnyVal

case class ApplicationFormQuestion(key: String, text: String, description: Option[String], helpText: Option[String])

case class ApplicationFormSection(sectionNumber: Int, title: String, questions: Seq[ApplicationFormQuestion], sectionType: String, fields: Seq[Field]) {
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
