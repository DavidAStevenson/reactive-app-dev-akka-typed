name := "TouristAndGuidebook"
version := "1.0"
scalaVersion := "2.13.1"

lazy val akkaVersion = "2.6.3"

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "org.scalatest" %% "scalatest" % "3.1.0" % "test"
)
