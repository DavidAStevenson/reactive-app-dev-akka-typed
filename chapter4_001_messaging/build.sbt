lazy val akkaVersion = "2.6.6"
lazy val scalaTestVersion = "3.1.2"
lazy val logbackVersion = "1.2.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest"     %% "scalatest"                % scalaTestVersion % Test,
  "ch.qos.logback"    %  "logback-classic"          % logbackVersion
)
