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

import com.pomadchin.domain.Extent
import java.time.ZonedDateTime

package object syntax {
  implicit class QueryOps(val left: Query) {
    def and(right: Query): Query = And(left, right)
    def or(right: Query): Query  = Or(left, right)
  }

  def intersects(e: Extent): Query                         = Intersects(e)
  def contains(e: Extent): Query                           = Contains(e)
  def covers(e: Extent): Query                             = Covers(e)
  def at(t: ZonedDateTime): Query                          = At(t)
  def between(t1: ZonedDateTime, t2: ZonedDateTime): Query = Between(t1, t2)
  def all: Query                                           = All()
  def empty: Query                                         = Empty()
}
