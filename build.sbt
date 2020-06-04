lazy val commonSettings = Seq(
  scalaVersion := "2.13.1"
)

lazy val common = project.settings(commonSettings)

lazy val chapter4_001_messaging = project.dependsOn(common)
