name := "core"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.mongodb" % "mongo-java-driver" % "2.12.4",
  "org.mongodb.morphia" % "morphia" % "0.108",
  "org.mongodb.morphia" % "morphia-validation" % "0.108",
  "cglib" % "cglib-nodep" % "3.1",
  "com.thoughtworks.proxytoys" % "proxytoys" % "1.0",
  "org.apache.solr" % "solr-solrj" % "4.10.0",
  "dom4j" % "dom4j" % "1.6.1",
  "jaxen" % "jaxen" % "1.1.6",
  "commons-codec" % "commons-codec" % "20041127.091804"
  //  "commons-logging" % "commons-logging" % "1.2"
)
