val pName = "akkaclusterexample"

lazy val commonSettings = Seq(
  // Note be careful of moving this as it gets parsed to find the version number by the publish scripts
  version := "1.3.0.1",
  name := pName,
  packageName in Universal := pName,
  organization := "com.tomliddle",
  scalaVersion := "2.11.8",
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  scalacOptions += "-language:postfixOps",
  coverageExcludedPackages := "<empty>;controllers\\.javascript;controllers\\.ref;views.*;",
  scalastyleConfig in Compile := baseDirectory.value / "project" / "scalastyle_config.xml"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
  .settings(commonSettings)

val AkkaVersion = "2.5.14"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "3.0.0",
  "com.typesafe.akka" %% "akka-persistence" % AkkaVersion,
  "org.iq80.leveldb" % "leveldb" % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
  "com.typesafe.akka" %% "akka-cluster" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % AkkaVersion,
  "pl.immutables" %% "akka-reasonable-downing" % "1.1.0",
  guice,
  ws,
  // Test libs
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test,
  "org.mockito" % "mockito-core" % "2.18.3" % Test
)
