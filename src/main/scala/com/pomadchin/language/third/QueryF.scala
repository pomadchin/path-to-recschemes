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

package com.pomadchin.language.third

import com.pomadchin.domain._

import cats.Functor
import io.circe._
import io.circe.syntax._
import io.circe.generic.JsonCodec
import higherkindness.droste.{Algebra, Coalgebra, scheme}
import higherkindness.droste.data.Fix
import jp.ne.opt.chronoscala.Imports._

import java.time.ZonedDateTime

// In this package we're replicating everything that was done in the "second" package, but with droste types
/** The type is often called a "pattern functor". */
@JsonCodec sealed trait QueryF[A]
@JsonCodec case class And[A](l: A, r: A) extends QueryF[A]
@JsonCodec case class Or[A](l: A, r: A) extends QueryF[A]
@JsonCodec case class Intersects[A](extent: Extent) extends QueryF[A]
@JsonCodec case class Contains[A](extent: Extent) extends QueryF[A]
@JsonCodec case class Covers[A](extent: Extent) extends QueryF[A]
@JsonCodec case class At[A](t: ZonedDateTime) extends QueryF[A]
@JsonCodec case class Between[A](t1: ZonedDateTime, t2: ZonedDateTime) extends QueryF[A]
@JsonCodec case class All[A]() extends QueryF[A]
@JsonCodec case class Nothing[A]() extends QueryF[A]

object QueryF {
  /** To make our types look better (at least a little bit better). */
  type Query = Fix[QueryF]

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
      case Nothing()     => Nothing[B]()
      case All()         => All[B]()
    }
  }

  /** Algebra to fold into some random String */
  val algebraString: Algebra[QueryF, String] = Algebra {
    case Intersects(e)   => s"(intersects $e)"
    case Contains(e)     => s"(contains $e)"
    case Covers(e)       => s"(covers $e)"
    case At(t)           => s"(at $t)"
    case Between(t1, t2) => s"(between ($t1 & $t2))"
    case All()           => "(all)"
    case Nothing()       => "(nothing)"
    case And(e1, e2)     => s"($e1 and $e2)"
    case Or(e1, e2)      => s"($e1 or $e2)"
  }

  /** Algebra to evaluate expression on a List */
  val algebraList: Algebra[QueryF, List[Raster] => List[Raster]] = Algebra {
    case Intersects(e)   => _.filter(_.extent.intersects(e))
    case Contains(e)     => _.filter(_.extent.contains(e))
    case Covers(e)       => _.filter(_.extent.covers(e))
    case At(t)           => _.filter(_.metadata.time.contains(t))
    case Between(t1, t2) => _.filter(_.metadata.time.fold(false) { current => t1 <= current && current < t2 })
    case All()           => identity
    case Nothing()       => _ => Nil
    case And(e1, e2)     => list => val left = e1(list); e2(left)
    case Or(e1, e2)      => list => e1(list) ++ e2(list)
  }

  /** Folding Query expression into Json */
  val algebraJson: Algebra[QueryF, Json] = Algebra(_.asJson)

  val unfolder: Json.Folder[QueryF[Json]] = new Json.Folder[QueryF[Json]] {
    def onNull: QueryF[Json]                       = Nothing()
    def onBoolean(value: Boolean): QueryF[Json]    = Nothing()
    def onNumber(value: JsonNumber): QueryF[Json]  = Nothing()
    def onString(value: String): QueryF[Json]      = Nothing()
    def onArray(value: Vector[Json]): QueryF[Json] = Nothing()
    def onObject(value: JsonObject): QueryF[Json]  =
      value
        .asJson
        .as[QueryF[Json]]
        .getOrElse(Nothing[Json]())
  }

  /** Unfolding JSON into a Query expression */
  val coalgebraJson: Coalgebra[QueryF, Json] = Coalgebra(_.foldWith(unfolder))

  def evalString(query: Query): String                     = scheme.cata(algebraString).apply(query)
  def evalList(query: Query): List[Raster] => List[Raster] = scheme.cata(algebraList).apply(query)

  /** Compiler can't just derive JSON Codecs now */
  def asJson(query: Query): Json  = scheme.cata(algebraJson).apply(query)
  def fromJson(json: Json): Query = scheme.ana(coalgebraJson).apply(json)
}