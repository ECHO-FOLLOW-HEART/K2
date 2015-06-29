import com.typesafe.sbt.SbtAspectj.AspectjKeys._
import com.typesafe.sbt.SbtAspectj._

name := "k2"

version := "3.0"

lazy val `core` = (project in file("modules/core")).enablePlugins(PlayScala)

lazy val `app` = (project in file("modules/app")).enablePlugins(PlayScala).dependsOn(core)

lazy val `web` = (project in file("modules/web")).enablePlugins(PlayScala).dependsOn(core)

lazy val `k2` = (project in file(".")).enablePlugins(PlayScala)
  .dependsOn(core)
  .dependsOn(app)
  .dependsOn(web)
  .aggregate(core, app, web)

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  filters,
  "org.mongodb" % "mongo-java-driver" % "3.0.0",
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
  "com.twitter" %% "finagle-thrift" % "6.25.0"
)

javaOptions ++= Seq("-Xmx2048M", "-XX:MaxPermSize=2048M")

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

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
