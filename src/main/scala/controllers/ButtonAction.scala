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

case object Skip extends ButtonAction {
  override val name: String = "_skip_item_button"
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
      case Skip.name => Some(Skip)
      case _ => None
    }
  }
}
