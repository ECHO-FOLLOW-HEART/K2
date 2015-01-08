import com.typesafe.sbt.SbtAspectj.AspectjKeys._
import com.typesafe.sbt.SbtAspectj._

name := "taozi"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.aspectj" % "aspectjrt" % "1.8.2",
  "org.aspectj" % "aspectjweaver" % "1.8.4",
  "org.mongodb" % "mongo-java-driver" % "2.12.4",
  "org.springframework" % "spring-aspects" % "3.2.2.RELEASE",
  "org.springframework" % "spring-aop" % "3.2.2.RELEASE",
  "org.springframework" % "spring-tx" % "3.2.2.RELEASE",
  "javax.persistence" % "persistence-api" % "1.0.2"
//  "org.mongodb" % "mongo-java-driver" % "2.12.4",
//  "org.mongodb.morphia" % "morphia" % "0.108",
//  "org.mongodb.morphia" % "morphia-validation" % "0.108",
//  "cglib" % "cglib-nodep" % "3.1",
//  "com.thoughtworks.proxytoys" % "proxytoys" % "1.0",
//  "org.apache.solr" % "solr-solrj" % "4.10.0"
//  //  "commons-logging" % "commons-logging" % "1.2"
)

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

binaries in Aspectj <++= update map { report =>
  report.matching(
    moduleFilter(organization = "javax.persistence", name = "persistence-api")
  )
}

products in Compile <<= products in Aspectj

products in Runtime <<= products in Compile
