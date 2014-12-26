name := "k2"

version := "2.0"

lazy val `core` = (project in file("modules/core")).enablePlugins(PlayJava)

lazy val `taozi` = (project in file("modules/taozi")).enablePlugins(PlayJava).dependsOn(core)

lazy val `web` = (project in file("modules/web")).enablePlugins(PlayJava).dependsOn(core)

lazy val `k2` = (project in file(".")).enablePlugins(PlayJava)
  .dependsOn(core)
  .dependsOn(taozi)
  .dependsOn(web)
  .aggregate(core, taozi, web)

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  filters,
  "org.mongodb" % "mongo-java-driver" % "2.12.4",
  play.PlayImport.cache,
  "com.github.mumoshu" %% "play2-memcached" % "0.6.0"
)



javaOptions ++= Seq("-Xmx2048M", "-XX:MaxPermSize=2048M")

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.