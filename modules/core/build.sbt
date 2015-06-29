import com.typesafe.sbt.SbtAspectj.AspectjKeys._

name := "core"

scalaVersion := "2.10.3"

val morphiaVersion = "0.111"

libraryDependencies ++= Seq(
  "org.mongodb" % "mongo-java-driver" % "3.0.0",
  "org.mongodb.morphia" % "morphia" % morphiaVersion,
  "org.mongodb.morphia" % "morphia-validation" % morphiaVersion,
  "org.hibernate" % "hibernate-validator" % "5.1.3.Final",
  "javax.el" % "javax.el-api" % "3.0.0",
  "cglib" % "cglib-nodep" % "3.1",
  "com.thoughtworks.proxytoys" % "proxytoys" % "1.0",
  "org.apache.solr" % "solr-solrj" % "4.10.0",
  "dom4j" % "dom4j" % "1.6.1",
  "jaxen" % "jaxen" % "1.1.6",
  "com.lvxingpai" %% "appconfig" % "0.2.1-SNAPSHOT",
  "org.aspectj" % "aspectjrt" % "1.8.4",
  "org.aspectj" % "aspectjweaver" % "1.8.4",
  "org.springframework" % "spring-aspects" % "3.2.2.RELEASE",
  "org.springframework" % "spring-aop" % "3.2.2.RELEASE",
  "org.springframework" % "spring-tx" % "3.2.2.RELEASE",
  "javax.persistence" % "persistence-api" % "1.0.2",
  play.PlayImport.cache,
  "com.github.mumoshu" %% "play2-memcached" % "0.6.0"
  //  "commons-logging" % "commons-logging" % "1.2"
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


