name := """aizou"""

version := "1.3"

lazy val core = (project in file("modules/core")).enablePlugins(PlayJava)

lazy val web = (project in file("modules/web")).enablePlugins(PlayJava).dependsOn(core)

lazy val travelpi = (project in file("modules/travelpi")).enablePlugins(PlayJava).dependsOn(core)

lazy val taozi = (project in file("modules/taozi")).enablePlugins(PlayJava).dependsOn(core)

lazy val root = (project in file(".")).enablePlugins(PlayJava)
  .dependsOn(core).dependsOn(web).dependsOn(travelpi).dependsOn(taozi)
  .aggregate(core, web, taozi, travelpi)

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  filters,
  "org.mongodb" % "mongo-java-driver" % "2.12.4"
)

javaOptions ++= Seq("-Xmx2048M", "-XX:MaxPermSize=2048M")
