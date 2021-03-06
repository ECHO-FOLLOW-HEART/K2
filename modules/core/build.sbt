import com.typesafe.sbt.SbtAspectj.AspectjKeys._

name := "core"

scalaVersion := "2.11.4"

val morphiaVersion = "1.0.0"

val springVersion = "3.2.2.RELEASE"
//val springVersion = "4.1.6.RELEASE"

val finagleVersion = "6.30.0"

libraryDependencies ++= Seq(
//  "org.mongodb" % "mongo-java-driver" % "3.0.1",
  "org.mongodb.morphia" % "morphia" % morphiaVersion,
  "org.mongodb.morphia" % "morphia-validation" % morphiaVersion,
  "org.hibernate" % "hibernate-validator" % "5.1.3.Final",
  "javax.el" % "javax.el-api" % "3.0.0",
  //"cglib" % "cglib-nodep" % "3.1",
  "com.thoughtworks.proxytoys" % "proxytoys" % "1.0",
  "org.apache.solr" % "solr-solrj" % "4.10.0",
  "dom4j" % "dom4j" % "1.6.1",
  "jaxen" % "jaxen" % "1.1.6",
  "com.lvxingpai" %% "appconfig" % "0.2.1-SNAPSHOT",
  //"org.aspectj" % "aspectjrt" % "1.8.2",
  //"org.aspectj" % "aspectjweaver" % "1.8.2",
  "org.springframework" % "spring-aspects" % springVersion,
  "org.springframework" % "spring-aop" % springVersion,
  "org.springframework" % "spring-tx" % springVersion,
  "javax.persistence" % "persistence-api" % "1.0.2",
  play.sbt.PlayImport.cache,
  "com.github.mumoshu" %% "play2-memcached" % "0.6.0",

  "com.twitter" %% "finagle-thriftmux" % finagleVersion,
  "com.twitter" %% "finagle-http" % finagleVersion,
  "com.twitter" %% "scrooge-core" % "4.2.0",
  "com.lvxingpai" %% "core-model" % "0.2.1.13",
  "org.elasticsearch" % "elasticsearch" % "1.7.1"
  //  "commons-logging" % "commons-logging" % "1.2"
)

com.twitter.scrooge.ScroogeSBT.newSettings

scalariformSettings

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


