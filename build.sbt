name := """k2"""

version := "3.0-SNAPSHOT"

//lazy val root = (project in file(".")).enablePlugins(PlayScala)

lazy val `core` = (project in file("modules/core")).enablePlugins(PlayScala)

lazy val `app` = (project in file("modules/app")).enablePlugins(PlayScala).dependsOn(core)

// lazy val `web` = (project in file("modules/web")).enablePlugins(PlayScala).dependsOn(core)

lazy val `k2` = (project in file(".")).enablePlugins(PlayScala)
  .dependsOn(core)
  .dependsOn(app)
  // .dependsOn(web)
  //.aggregate(core, app, web)
  .aggregate(core, app)

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,

  "joda-time" % "joda-time" % "2.8.1"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

scalariformSettings

// AspectJ settings

import com.typesafe.sbt.SbtAspectj.AspectjKeys._
import com.typesafe.sbt.SbtAspectj._

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
