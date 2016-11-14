package forms.validation

import models.ApplicationFormSection

case class SectionError(formSection:ApplicationFormSection, msg: String)

