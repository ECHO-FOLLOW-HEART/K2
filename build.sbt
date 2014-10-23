name := """com.lvxingpai.api"""

version := "1.3"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.10.3"

//conflictWarning := ConflictWarning.disable

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  //"mysql" % "mysql-connector-java" % "latest.integration",
  "org.mongodb" % "mongo-java-driver" % "2.12.4",
  "org.mongodb.morphia" % "morphia" % "0.108",
  "org.mongodb.morphia" % "morphia-validation" % "0.108",
  "cglib" % "cglib-nodep" % "3.1",
  "com.thoughtworks.proxytoys" % "proxytoys" % "1.0",
//  "com.vxp" % "plan_2.10" % "1.0.1",
  "org.apache.solr" % "solr-solrj" % "4.10.0",
  "commons-logging" % "commons-logging" % "1.2",
  filters
)

//externalPom(Def.setting(baseDirectory.value / "morphia-validation.xml"))
javaOptions ++= Seq("-Xmx2048M", "-XX:MaxPermSize=2048M")
