resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "sonatype" at "https://oss.sonatype.org/content/groups/public"
)


// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.0")

// web plugins

addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.0.0")

// sbt-scrooge
//An SBT plugin that adds a mixin for doing Thrift code auto-generation during your compile phase.
addSbtPlugin("com.twitter" %% "scrooge-sbt-plugin" % "3.14.1")


// AspectJ

addSbtPlugin("com.typesafe.sbt" % "sbt-aspectj" % "0.10.0")
