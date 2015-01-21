import com.typesafe.sbt.SbtAspectj.AspectjKeys._
import com.typesafe.sbt.SbtAspectj._

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
  "org.springframework" % "spring-aspects" % "3.2.2.RELEASE",
  "org.springframework" % "spring-aop" % "3.2.2.RELEASE",
  "org.springframework" % "spring-tx" % "3.2.2.RELEASE",
  "org.aspectj" % "aspectjrt" % "1.8.2",
  "org.aspectj" % "aspectjweaver" % "1.8.4",
  "com.aizou" % "iisfileappender_2.9.1" % "0.1"
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
