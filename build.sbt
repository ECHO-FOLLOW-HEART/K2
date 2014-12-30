import com.typesafe.sbt.SbtAspectj.{Aspectj, aspectjSettings, compiledClasses}
import com.typesafe.sbt.SbtAspectj.AspectjKeys.{binaries, inputs, lintProperties}

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
  "org.springframework" % "spring-aspects" % "3.2.2.RELEASE"
)

javaOptions ++= Seq("-Xmx2048M", "-XX:MaxPermSize=2048M")

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

aspectjSettings

inputs in Aspectj <+= compiledClasses

binaries in Aspectj <++= update map { report =>
  report.matching(
    moduleFilter(organization = "org.springframework", name = "spring-aspects")
  )
}

products in Compile <<= products in Aspectj

products in Runtime <<= products in Compile