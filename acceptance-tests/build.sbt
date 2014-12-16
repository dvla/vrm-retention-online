import Common._

name := "vrm-retention-acceptance-tests"

version := versionString

organization := organisationString

organizationName := organisationNameString

scalaVersion := scalaVersionString

scalacOptions := scalaOptionsSeq

publishTo.<<=(publishResolver)

credentials += sbtCredentials

resolvers ++= projectResolvers

libraryDependencies ++= Seq(
  "info.cukes" %% "cucumber-scala" % "1.2.0" % "test" withSources() withJavadoc(),
  "info.cukes" % "cucumber-junit" % "1.2.0" % "test" withSources() withJavadoc(),
  "info.cukes" % "cucumber-picocontainer" % "1.2.0" % "test" withSources() withJavadoc(),
  "com.novocode" % "junit-interface" % "0.10" % "test",
  "com.typesafe" % "config" % "1.2.1" % "test"
)
