name := """com.lvxingpai.api"""

version := "1.1"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.10.3"

conflictWarning := ConflictWarning.disable

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "mysql" % "mysql-connector-java" % "latest.integration",
  "org.mongodb" % "mongo-java-driver" % "latest.integration",
  "org.mongodb.morphia" % "morphia" % "latest.integration",
  "org.mongodb.morphia" % "morphia-validation" % "latest.integration",
  "cglib" % "cglib-nodep" % "latest.integration",
  "com.thoughtworks.proxytoys" % "proxytoys" % "latest.integration",
//  "com.vxp" % "plan_2.10" % "1.0.1",
  filters
)

//externalPom(Def.setting(baseDirectory.value / "morphia-validation.xml"))
