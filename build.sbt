name := "TouristAndGuidebook"
version := "1.0"
scalaVersion := "2.13.1"

lazy val akkaVersion = "2.6.3"

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
    "ch.qos.logback"     % "logback-classic" % "1.2.3",
    "org.scalatest"     %% "scalatest" % "3.1.0" % "test"
)

Global / cancelable := false
