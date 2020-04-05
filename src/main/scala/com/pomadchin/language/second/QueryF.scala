/*
 * Copyright 2020 Grigory Pomadchin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pomadchin.language.second

import com.pomadchin.domain._

import cats.Functor
import cats.syntax.functor._
import io.circe._
import io.circe.syntax._
import io.circe.generic.JsonCodec
import jp.ne.opt.chronoscala.Imports._

import java.time.ZonedDateTime

@JsonCodec sealed trait QueryF[A]
@JsonCodec case class And[A](l: A, r: A) extends QueryF[A]
@JsonCodec case class Or[A](l: A, r: A) extends QueryF[A]
@JsonCodec case class Intersects[A](extent: Extent) extends QueryF[A]
@JsonCodec case class Contains[A](extent: Extent) extends QueryF[A]
@JsonCodec case class Covers[A](extent: Extent) extends QueryF[A]
@JsonCodec case class At[A](t: ZonedDateTime) extends QueryF[A]
@JsonCodec case class Between[A](t1: ZonedDateTime, t2: ZonedDateTime) extends QueryF[A]
@JsonCodec case class All[A]() extends QueryF[A]
@JsonCodec case class Empty[A]() extends QueryF[A]

/** Fix is used to help Scala compiler to derive an expression type. */
case class Fix[F[_]](unfix: F[Fix[F]])

object QueryF {
  /** To make our types look better (at least a little bit better). */
  type Query = Fix[QueryF]

  /** F-Algebra is a function that "folds" or "unpacks" an expression. */
  type Algebra[F[_], A] = F[A] => A

  /** Coalgebra is just an Algebra with a reversed arrow. It "unfolds" or "generates" an expression. */
  type Coalgebra[F[_], A] = A => F[A]

  /** cata aka foldRight */
  def cata[F[_]: Functor, A](algebra: F[A] => A)(fix: Fix[F]): A =
    algebra(fix.unfix.map(cata(algebra)(_)))

  /** ana aka unfold */
  def ana[F[_]: Functor, A](coalgebra: A => F[A])(a: A): Fix[F] =
    Fix(coalgebra(a).map(ana(coalgebra)(_)))

  /** hylo is a composition of ana and cata */
  def hylo[F[_]: Functor, A, B](algebra: Algebra[F, B], coalgebra: Coalgebra[F, A])(a: A): B =
    algebra(coalgebra(a).map(hylo(algebra, coalgebra)(_)))

  /** To map over the Expression tree the following Functor instance should be defined. */
  implicit val queryFFunctor: Functor[QueryF] = new Functor[QueryF] {
    def map[A, B](fa: QueryF[A])(f: A => B): QueryF[B] = fa match {
      case And(l, r)     => And(f(l), f(r))
      case Or(l, r)      => Or(f(l), f(r))
      case Intersects(v) => Intersects[B](v)
      case Contains(v)   => Contains[B](v)
      case Covers(v)     => Covers[B](v)
      case At(v)         => At[B](v)
      case Between(f, t) => Between[B](f, t)
      case Empty()       => Empty[B]()
      case All()         => All[B]()
    }
  }

  /** Algebra to fold into some random String */
  val algebraString: Algebra[QueryF, String] = {
    case Intersects(e)   => s"(intersects $e)"
    case Contains(e)     => s"(contains $e)"
    case Covers(e)       => s"(covers $e)"
    case At(t)           => s"(at $t)"
    case Between(t1, t2) => s"(between ($t1 & $t2))"
    case All()           => "(all)"
    case Empty()         => "(empty)"
    case And(e1, e2)     => s"($e1 and $e2)"
    case Or(e1, e2)      => s"($e1 or $e2)"
  }

  /** Algebra to evaluate expression on a List */
  val algebraList: Algebra[QueryF, List[Raster] => List[Raster]] = {
    case Intersects(e)   => _.filter(_.extent.intersects(e))
    case Contains(e)     => _.filter(_.extent.contains(e))
    case Covers(e)       => _.filter(_.extent.covers(e))
    case At(t)           => _.filter(_.metadata.time.contains(t))
    case Between(t1, t2) => _.filter(_.metadata.time.fold(false) { current => t1 <= current && current < t2 })
    case All()           => identity
    case Empty()         => _ => Nil
    case And(e1, e2)     => list => val left = e1(list); e2(left)
    case Or(e1, e2)      => list => e1(list) ++ e2(list)
  }

  /** Folding Query expression into Json */
  val algebraJson: Algebra[QueryF, Json] = _.asJson

  val unfolder: Json.Folder[QueryF[Json]] = new Json.Folder[QueryF[Json]] {
    def onNull: QueryF[Json]                       = Empty()
    def onBoolean(value: Boolean): QueryF[Json]    = Empty()
    def onNumber(value: JsonNumber): QueryF[Json]  = Empty()
    def onString(value: String): QueryF[Json]      = Empty()
    def onArray(value: Vector[Json]): QueryF[Json] = Empty()
    def onObject(value: JsonObject): QueryF[Json]  =
      value
        .asJson
        .as[QueryF[Json]]
        .getOrElse(Empty[Json]())
  }

  /** Unfolding JSON into a Query expression */
  val coalgebraJson: Coalgebra[QueryF, Json] = _.foldWith(unfolder)

  def evalString(query: Query): String                     = cata(algebraString)(query)
  def evalList(query: Query): List[Raster] => List[Raster] = cata(algebraList)(query)

  /** Compiler can't just derive JSON Codecs now */
  def asJson(query: Query): Json  = cata(algebraJson)(query)
  def fromJson(json: Json): Query = ana(coalgebraJson)(json)
}