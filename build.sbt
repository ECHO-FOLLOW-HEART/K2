name := """com.lvxingpai.api"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

conflictWarning := ConflictWarning.disable

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "mysql" % "mysql-connector-java" % "latest.integration",
  "org.mongodb" % "mongo-java-driver" % "latest.integration",
  "org.mongodb.morphia" % "morphia" % "latest.integration",
//  "com.vxp" % "plan_2.10" % "1.0.1",
  filters
)
