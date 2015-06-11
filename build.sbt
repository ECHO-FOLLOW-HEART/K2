import com.typesafe.sbt.SbtAspectj.AspectjKeys._
import com.typesafe.sbt.SbtAspectj._

name := "aizou"

version := "2.0"

lazy val `core` = (project in file("modules/core")).enablePlugins(PlayJava, ScroogeSBT)

lazy val `app` = (project in file("modules/app")).enablePlugins(PlayJava, ScroogeSBT).dependsOn(core)

lazy val `web` = (project in file("modules/web")).enablePlugins(PlayJava, ScroogeSBT).dependsOn(core)

lazy val `k2` = (project in file(".")).enablePlugins(PlayJava, ScroogeSBT)
  .dependsOn(core)
  .dependsOn(app)
  .dependsOn(web)
  .aggregate(core, app, web)

scalaVersion := "2.10.3"

val finagleVersion = "6.25.0"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  filters,
  "org.mongodb" % "mongo-java-driver" % "2.12.4",
  "org.springframework" % "spring-aspects" % "3.2.2.RELEASE",
  "org.springframework" % "spring-aop" % "3.2.2.RELEASE",
  "org.springframework" % "spring-tx" % "3.2.2.RELEASE",
  "org.aspectj" % "aspectjrt" % "1.8.4",
  "org.aspectj" % "aspectjweaver" % "1.8.4",
  "com.lvxingpai" %% "fileappender" % "0.1-SNAPSHOT",
  play.PlayImport.cache,
  "com.github.mumoshu" %% "play2-memcached" % "0.6.0",
  "org.apache.thrift" % "libthrift" % "0.9.2",
  "com.twitter" %% "scrooge-core" % "3.18.1",
  "com.twitter" %% "finagle-thrift" % finagleVersion,
  "com.twitter" %% "finagle-core" % finagleVersion,
  "com.twitter" %% "finagle-thrift" % finagleVersion,
  "com.twitter" %% "finagle-thriftmux" % finagleVersion
)

javaOptions ++= Seq("-Xmx2048M", "-XX:MaxPermSize=2048M")

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

aspectjSettings

showWeaveInfo in Aspectj := false

inputs in Aspectj <+= compiledClasses


binaries in Aspectj <++= update map { report =>
  report.matching(
    moduleFilter(organization = "org.springframework", name = "spring-aspects")
  )
}

binaries in Aspectj <++= update map { report =>
  report.matching(
    moduleFilter(organization = "org.springframework", name = "spring-aop")
  )
}

binaries in Aspectj <++= update map { report =>
  report.matching(
    moduleFilter(organization = "org.springframework", name = "spring-tx")
  )
}

products in Compile <<= products in Aspectj

products in Runtime <<= products in Compile
