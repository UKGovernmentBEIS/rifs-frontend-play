package controllers

sealed trait ButtonAction

case object Complete extends ButtonAction

case object Save extends ButtonAction

case object Preview extends ButtonAction

object ButtonAction {
  def unapply(s: String): Option[ButtonAction] = {
    s match {
      case "_complete_button" => Some(Complete)
      case "_save_button" => Some(Save)
      case "_preview_button" => Some(Preview)
      case _ => None
    }
  }
}
