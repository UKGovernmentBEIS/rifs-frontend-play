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

import eu.timepit.refined.api.{RefType, Validate}
import play.api.libs.json.{JsError, JsSuccess, Reads, Writes}
import play.api.mvc.{PathBindable, QueryStringBindable}
import scala.language.higherKinds

package object controllers {
  trait RefinedBinders {
    implicit def writeRefined[T, P, F[_, _]](
                                              implicit writesT: Writes[T],
                                              reftype: RefType[F]
                                            ): Writes[F[T, P]] = Writes(value => writesT.writes(reftype.unwrap(value)))

    implicit def readRefined[T, P, F[_, _]](
                                             implicit readsT: Reads[T],
                                             reftype: RefType[F],
                                             validate: Validate[T, P]
                                           ): Reads[F[T, P]] =
      Reads(jsValue =>
        readsT.reads(jsValue).flatMap { valueT =>
          reftype.refine[P](valueT) match {
            case Right(valueP) => JsSuccess(valueP)
            case Left(error) => JsError(error)
          }
        })

    implicit def pathBindRefined[T, P, F[_, _]](
                                                 implicit
                                                 pathBindT: PathBindable[T],
                                                 reftype: RefType[F],
                                                 validate: Validate[T, P]
                                               ): PathBindable[F[T, P]] =
      new PathBindable[F[T, P]] {
        override def unbind(key: String, value: F[T, P]): String =
          pathBindT.unbind(key, reftype.unwrap(value))

        override def bind(key: String, value: String): Either[String, F[T, P]] =
          pathBindT.bind(key, value).right.flatMap(reftype.refine(_)(validate))
      }

    implicit def queryBindRefined[T, P, F[_, _]](
                                                  implicit
                                                  queryBindT: QueryStringBindable[T],
                                                  reftype: RefType[F],
                                                  validate: Validate[T, P]
                                                ): QueryStringBindable[F[T, P]] = new QueryStringBindable[F[T, P]] {
      override def unbind(key: String, value: F[T, P]) =
        queryBindT.unbind(key, reftype.unwrap(value))

      override def bind(key: String, params: Map[String, Seq[String]]) =
        queryBindT.bind(key, params).map(_.right.flatMap(reftype.refine(_)(validate)))
    }
  }

  object RefinedBinders extends RefinedBinders
}
