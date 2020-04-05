name := "path-to-recschemes"
version := "0.1.0-SNAPSHOT"
scalaVersion := "2.13.1"
organization := "com.pomadchin"
scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-language:higherKinds",
  "-language:postfixOps",
  "-language:existentials",
  "-feature",
  "-Ymacro-annotations"
)

organizationName := "Grigory Pomadchin"
startYear := Some(2020)
licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))

libraryDependencies ++= Seq(
  "io.higherkindness" %% "droste-core"   % "0.8.0",
  "io.circe"          %% "circe-generic" % "0.13.0",
  "io.circe"          %% "circe-core"    % "0.13.0",
  "io.circe"          %% "circe-parser"  % "0.13.0",
  "jp.ne.opt"         %% "chronoscala"   % "0.3.2",
  "org.scalatest"     %% "scalatest"     % "3.1.1" % Test
)
