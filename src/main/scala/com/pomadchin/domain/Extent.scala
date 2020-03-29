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

package com.pomadchin.domain

import io.circe.generic.JsonCodec

/** Extent represents corner coordinates of a [[Tile]]. */
@JsonCodec
case class Extent(xmin: Double, ymin: Double, xmax: Double, ymax: Double) {
  def intersects(other: Extent): Boolean = !(other.xmax < xmin || other.xmin > xmax) && !(other.ymax < ymin || other.ymin > ymax)

  def contains(other: Extent): Boolean =
    if(xmin == 0 && xmax == 0 && ymin == 0 && ymax == 0) false
    else other.xmin >= xmin && other.ymin >= ymin && other.xmax <= xmax && other.ymax <= ymax

  def contains(x: Double, y: Double): Boolean = x > xmin && x < xmax && y > ymin && y < ymax

  def covers(other: Extent): Boolean = contains(other)
}
