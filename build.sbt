lazy val commonSettings = Seq(
  scalaVersion := "2.13.1",
  scalacOptions += "-deprecation"
)

lazy val chapter2 = project.settings(commonSettings)

lazy val chapter4_001_messaging = project.settings(commonSettings)
lazy val chapter4_002_elasticity = project.settings(commonSettings)
