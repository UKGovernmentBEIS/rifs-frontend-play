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

import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.{NonNegative, Positive}

package object models {
  type LongId = Long Refined Positive

  type NonEmptyString = String Refined NonEmpty
  type NonNegativeInt = Int Refined NonNegative
  type PosInt = Int Refined Positive

  implicit val longIdOrd = new Ordering[LongId] {
    override def compare(x: LongId, y: LongId): Int = implicitly[Ordering[Long]].compare(x.value, y.value)
  }

  implicit val posIntOrd = new Ordering[PosInt] {
    override def compare(x: PosInt, y: PosInt): Int = implicitly[Ordering[Int]].compare(x.value, y.value)
  }
}
