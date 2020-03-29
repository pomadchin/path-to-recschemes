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
import com.pomadchin.language.first.syntax._

import io.circe.syntax._
import io.circe.parser._
import cats.syntax.either._

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.time.ZonedDateTime

class QuerySpec extends AnyFunSpec with Matchers {
  describe("QuerySpec") {
    it("Query.evalString") {
      val t: ZonedDateTime = ZonedDateTime.parse("2020-03-27T20:00:00.591Z")
      val expr = (intersects(Extent(0, 0, 4, 4)) and covers(Extent(1, 1, 3, 3)) or intersects(Extent(2, 2, 5, 5))) and at(t)

      Query.evalString(expr) shouldBe "((((intersects Extent(0.0,0.0,4.0,4.0)) and (covers Extent(1.0,1.0,3.0,3.0))) or (intersects Extent(2.0,2.0,5.0,5.0))) and (at 2020-03-27T20:00:00.591Z))"
    }

    it("Query.evalList") {
      val catalog =
        Raster(Tile(), Extent(0, 0, 4, 4), Metadata(Map("time" -> "2020-03-20T20:00:00.591Z"))) ::
        Raster(Tile(), Extent(1, 1, 5, 5), Metadata(Map("time" -> "2020-03-21T20:00:00.591Z"))) ::
        Raster(Tile(), Extent(2, 2, 5, 5), Metadata(Map("time" -> "2020-03-22T20:00:00.591Z"))) ::
        Raster(Tile(), Extent(2, 2, 6, 6), Metadata(Map("time" -> "2020-03-22T20:00:00.591Z"))) ::
        Raster(Tile(), Extent(10, 10, 11, 11), Metadata(Map("time" -> "2020-03-23T20:00:00.591Z"))) ::
        Raster(Tile(), Extent(1, 1, 4, 4), Metadata(Map("time" -> "2020-03-20T20:00:00.591Z"))) ::Nil

      val t: ZonedDateTime = ZonedDateTime.parse("2020-03-20T20:00:00.591Z")
      val expr = (intersects(Extent(0, 0, 4, 4)) and covers(Extent(1, 1, 3, 3)) or intersects(Extent(2, 2, 5, 5))) and at(t)

      Query.evalList(expr)(catalog) shouldBe List(
        Raster(Tile(), Extent(0.0,0.0,4.0,4.0), Metadata(Map("time" -> "2020-03-20T20:00:00.591Z"))),
        Raster(Tile(), Extent(1.0,1.0,4.0,4.0), Metadata(Map("time" -> "2020-03-20T20:00:00.591Z"))),
        Raster(Tile(), Extent(0.0,0.0,4.0,4.0), Metadata(Map("time" -> "2020-03-20T20:00:00.591Z"))),
        Raster(Tile(), Extent(1.0,1.0,4.0,4.0), Metadata(Map("time" -> "2020-03-20T20:00:00.591Z")))
      )
    }

    it("Query.toJson") {
      val t: ZonedDateTime = ZonedDateTime.parse("2020-03-20T20:00:00.591Z")
      val expr = (intersects(Extent(0, 0, 4, 4)) and covers(Extent(1, 1, 3, 3)) or intersects(Extent(2, 2, 5, 5))) and at(t)
      expr.asJson shouldBe parse(
        """
          |{
          |    "And" : {
          |        "l" : {
          |            "Or" : {
          |                "l" : {
          |                    "And" : {
          |                        "l" : {
          |                            "Intersects" : {
          |                                "extent" : {
          |                                    "xmin" : 0.0,
          |                                    "ymin" : 0.0,
          |                                    "xmax" : 4.0,
          |                                    "ymax" : 4.0
          |                                }
          |                            }
          |                        },
          |                        "r" : {
          |                            "Covers" : {
          |                                "extent" : {
          |                                    "xmin" : 1.0,
          |                                    "ymin" : 1.0,
          |                                    "xmax" : 3.0,
          |                                    "ymax" : 3.0
          |                                }
          |                            }
          |                        }
          |                    }
          |                },
          |                "r" : {
          |                    "Intersects" : {
          |                        "extent" : {
          |                            "xmin" : 2.0,
          |                            "ymin" : 2.0,
          |                            "xmax" : 5.0,
          |                            "ymax" : 5.0
          |                        }
          |                    }
          |                }
          |            }
          |        },
          |        "r" : {
          |            "At" : {
          |                "t" : "2020-03-20T20:00:00.591Z"
          |            }
          |        }
          |    }
          |}
          |""".stripMargin).valueOr(throw _)
    }

    it("Query.fromJson") {
      val t: ZonedDateTime = ZonedDateTime.parse("2020-03-20T20:00:00.591Z")
      val expr = (intersects(Extent(0, 0, 4, 4)) and covers(Extent(1, 1, 3, 3)) or intersects(Extent(2, 2, 5, 5))) and at(t)

      parse(
        """
          |{
          |    "And" : {
          |        "l" : {
          |            "Or" : {
          |                "l" : {
          |                    "And" : {
          |                        "l" : {
          |                            "Intersects" : {
          |                                "extent" : {
          |                                    "xmin" : 0.0,
          |                                    "ymin" : 0.0,
          |                                    "xmax" : 4.0,
          |                                    "ymax" : 4.0
          |                                }
          |                            }
          |                        },
          |                        "r" : {
          |                            "Covers" : {
          |                                "extent" : {
          |                                    "xmin" : 1.0,
          |                                    "ymin" : 1.0,
          |                                    "xmax" : 3.0,
          |                                    "ymax" : 3.0
          |                                }
          |                            }
          |                        }
          |                    }
          |                },
          |                "r" : {
          |                    "Intersects" : {
          |                        "extent" : {
          |                            "xmin" : 2.0,
          |                            "ymin" : 2.0,
          |                            "xmax" : 5.0,
          |                            "ymax" : 5.0
          |                        }
          |                    }
          |                }
          |            }
          |        },
          |        "r" : {
          |            "At" : {
          |                "t" : "2020-03-20T20:00:00.591Z"
          |            }
          |        }
          |    }
          |}
          |""".stripMargin).flatMap(_.as[Query]).valueOr(throw _) shouldBe expr
    }

    it("Query evaluate from JSON") {
      val catalog =
        Raster(Tile(), Extent(0, 0, 4, 4), Metadata(Map("time" -> "2020-03-20T20:00:00.591Z"))) ::
        Raster(Tile(), Extent(1, 1, 5, 5), Metadata(Map("time" -> "2020-03-21T20:00:00.591Z"))) ::
        Raster(Tile(), Extent(2, 2, 5, 5), Metadata(Map("time" -> "2020-03-22T20:00:00.591Z"))) ::
        Raster(Tile(), Extent(2, 2, 6, 6), Metadata(Map("time" -> "2020-03-22T20:00:00.591Z"))) ::
        Raster(Tile(), Extent(10, 10, 11, 11), Metadata(Map("time" -> "2020-03-23T20:00:00.591Z"))) ::
        Raster(Tile(), Extent(1, 1, 4, 4), Metadata(Map("time" -> "2020-03-20T20:00:00.591Z"))) ::Nil

      val expr = parse(
        """
          |{
          |    "And" : {
          |        "l" : {
          |            "Or" : {
          |                "l" : {
          |                    "And" : {
          |                        "l" : {
          |                            "Intersects" : {
          |                                "extent" : {
          |                                    "xmin" : 0.0,
          |                                    "ymin" : 0.0,
          |                                    "xmax" : 4.0,
          |                                    "ymax" : 4.0
          |                                }
          |                            }
          |                        },
          |                        "r" : {
          |                            "Covers" : {
          |                                "extent" : {
          |                                    "xmin" : 1.0,
          |                                    "ymin" : 1.0,
          |                                    "xmax" : 3.0,
          |                                    "ymax" : 3.0
          |                                }
          |                            }
          |                        }
          |                    }
          |                },
          |                "r" : {
          |                    "Intersects" : {
          |                        "extent" : {
          |                            "xmin" : 2.0,
          |                            "ymin" : 2.0,
          |                            "xmax" : 5.0,
          |                            "ymax" : 5.0
          |                        }
          |                    }
          |                }
          |            }
          |        },
          |        "r" : {
          |            "At" : {
          |                "t" : "2020-03-20T20:00:00.591Z"
          |            }
          |        }
          |    }
          |}
          |""".stripMargin).flatMap(_.as[Query]).valueOr(throw _)

      Query.evalList(expr)(catalog) shouldBe List(
        Raster(Tile(), Extent(0.0,0.0,4.0,4.0), Metadata(Map("time" -> "2020-03-20T20:00:00.591Z"))),
        Raster(Tile(), Extent(1.0,1.0,4.0,4.0), Metadata(Map("time" -> "2020-03-20T20:00:00.591Z"))),
        Raster(Tile(), Extent(0.0,0.0,4.0,4.0), Metadata(Map("time" -> "2020-03-20T20:00:00.591Z"))),
        Raster(Tile(), Extent(1.0,1.0,4.0,4.0), Metadata(Map("time" -> "2020-03-20T20:00:00.591Z")))
      )
    }
  }
}
