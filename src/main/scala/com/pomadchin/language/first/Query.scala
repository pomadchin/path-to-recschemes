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

package com.pomadchin.language.first

import com.pomadchin.domain._

import io.circe.generic.JsonCodec
import jp.ne.opt.chronoscala.Imports._

import java.time.ZonedDateTime

@JsonCodec sealed trait Query
@JsonCodec case class And(l: Query, r: Query) extends Query
@JsonCodec case class Or(l: Query, r: Query) extends Query
@JsonCodec case class Intersects(extent: Extent) extends Query
@JsonCodec case class Contains(extent: Extent) extends Query
@JsonCodec case class Covers(extent: Extent) extends Query
@JsonCodec case class At(t: ZonedDateTime) extends Query
@JsonCodec case class Between(t1: ZonedDateTime, t2: ZonedDateTime) extends Query
@JsonCodec case class All() extends Query
@JsonCodec case class Empty() extends Query

object Query {
  def evalString(e: Query): String =
    e match {
      case Intersects(e)   => s"(intersects $e)"
      case Contains(e)     => s"(contains $e)"
      case Covers(e)       => s"(covers $e)"
      case At(t)           => s"(at $t)"
      case Between(t1, t2) => s"(between ($t1 & $t2))"
      case All()           => "(all)"
      case Empty()       => "(empty)"
      case And(e1, e2)     => s"(${evalString(e1)} and ${evalString(e2)})"
      case Or(e1, e2)      => s"(${evalString(e1)} or ${evalString(e2)})"
    }

  def evalList(e: Query): List[Raster] => List[Raster] =
    e match {
      case Intersects(e)   => _.filter(_.extent.intersects(e))
      case Contains(e)     => _.filter(_.extent.contains(e))
      case Covers(e)       => _.filter(_.extent.covers(e))
      case At(t)           => _.filter(_.metadata.time.contains(t))
      case Between(t1, t2) => _.filter(_.metadata.time.fold(false) { current => t1 <= current && current < t2 })
      case All()           => identity
      case Empty()       => _ => Nil
      case And(e1, e2)     => list => val left = evalList(e1)(list); evalList(e2)(left)
      case Or(e1, e2)      => list => evalList(e1)(list) ++ evalList(e2)(list)
    }
}
