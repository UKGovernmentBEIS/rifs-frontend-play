package controllers

sealed trait ButtonAction {
  def name: String
}

case object Complete extends ButtonAction {
  override val name: String = "_complete_button"
}

case object Save extends ButtonAction {
  override val name: String = "_save_button"
}

case object Preview extends ButtonAction {
  override val name: String = "_preview_button"
}

case object SaveItem extends ButtonAction {
  override val name: String = "_save_item_button"
}

case object completeAndPreview extends ButtonAction {
  override val name: String = "_complete_and_preview_button"
}

object ButtonAction {
  def unapply(s: String): Option[ButtonAction] = {
    s match {
      case Complete.name => Some(Complete)
      case Save.name => Some(Save)
      case Preview.name => Some(Preview)
      case SaveItem.name => Some(SaveItem)
      case completeAndPreview.name => Some(completeAndPreview)
      case _ => None
    }
  }
}
