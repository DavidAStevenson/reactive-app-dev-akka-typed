lazy val commonSettings = Seq(
  scalaVersion := "2.13.1",
  scalacOptions += "-deprecation"
)

lazy val chapter2 = project

lazy val chapter4_001_messaging = project.settings(commonSettings)
