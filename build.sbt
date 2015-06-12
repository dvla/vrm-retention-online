import Common._
import com.typesafe.sbt.web.SbtWeb
import org.scalastyle.sbt.ScalastylePlugin
import play.PlayScala
import uk.gov.dvla.vehicles.sandbox.ProjectDefinitions.legacyStubs
import uk.gov.dvla.vehicles.sandbox.ProjectDefinitions.osAddressLookup
import uk.gov.dvla.vehicles.sandbox.ProjectDefinitions.paymentSolve
import uk.gov.dvla.vehicles.sandbox.ProjectDefinitions.vehicleAndKeeperLookup
import uk.gov.dvla.vehicles.sandbox.ProjectDefinitions.vrmRetentionEligibility
import uk.gov.dvla.vehicles.sandbox.ProjectDefinitions.vrmRetentionRetain
import uk.gov.dvla.vehicles.sandbox.ProjectDefinitions.emailService
import uk.gov.dvla.vehicles.sandbox.Sandbox
import uk.gov.dvla.vehicles.sandbox.SandboxSettings
import uk.gov.dvla.vehicles.sandbox.Tasks
import io.gatling.sbt.GatlingPlugin
import GatlingPlugin.Gatling
import scoverage.ScoverageSbtPlugin.ScoverageKeys

publishTo <<= version { v: String =>
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at s"$nexus/snapshots")
  else
    Some("releases" at s"$nexus/releases")
}

name := "vrm-retention-online"

version := versionString

organization := organisationString

organizationName := organisationNameString

scalaVersion := scalaVersionString

scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-Xlint", "-language:reflectiveCalls", "-Xmax-classfile-name", "128")

crossScalaVersions := Seq("2.10.3", "2.11.4")

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

lazy val acceptanceTestsProject = Project("acceptance-tests", file("acceptance-tests"))
  .dependsOn(root % "test->test")
  .disablePlugins(PlayScala, SbtWeb)
  .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)

lazy val gatlingTestsProject = Project("gatling-tests", file("gatling-tests"))
  .disablePlugins(PlayScala, SbtWeb)
  .enablePlugins(GatlingPlugin)

pipelineStages := Seq(rjs, digest, gzip)

libraryDependencies ++= {
  val akkaVersion = "2.3.4"
  Seq(
    cache,
    filters,
    // The combination of selenium 2.43.0 and phantomjsdriver 1.2.0 works in the Travis build when open sourcing
    "org.seleniumhq.selenium" % "selenium-java" % "2.43.0" % "test",
    "com.github.detro" % "phantomjsdriver" % "1.2.0" % "test" withSources() withJavadoc(),
//    "com.codeborne" % "phantomjsdriver" % "1.2.1" % "test" withSources() withJavadoc(),
    "info.cukes" % "cucumber-java" % "1.2.0" % "test" withSources() withJavadoc(),
    "org.mockito" % "mockito-all" % "1.10.8" % "test" withSources() withJavadoc(),
    "com.github.tomakehurst" % "wiremock" % "1.51" % "test" withSources() withJavadoc() exclude("log4j", "log4j"),
    "org.slf4j" % "log4j-over-slf4j" % "1.7.7" % "test" withSources() withJavadoc(),
    "org.scalatest" %% "scalatest" % "2.2.2" % "test" withSources() withJavadoc(),
    "com.google.inject" % "guice" % "4.0-beta5" withSources() withJavadoc(),
    "com.google.guava" % "guava" % "18.0" withSources() withJavadoc(), // See: http://stackoverflow.com/questions/16614794/illegalstateexception-impossible-to-get-artifacts-when-data-has-not-been-loaded
    "com.tzavellas" % "sse-guice" % "0.7.1" withSources() withJavadoc(), // Scala DSL for Guice
    "commons-codec" % "commons-codec" % "1.8" withSources() withJavadoc(),
    "org.apache.httpcomponents" % "httpclient" % "4.3.6" withSources() withJavadoc(),
    "org.apache.pdfbox" % "pdfbox" % "1.8.6" withSources() withJavadoc(),
    "org.apache.pdfbox" % "preflight" % "1.8.6" withSources() withJavadoc(),
    "com.sun.mail" % "javax.mail" % "1.5.2",
    "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.0",
    "dvla" %% "vehicles-presentation-common" % "2.26-SNAPSHOT" withSources() withJavadoc() exclude("junit", "junit-dep"),
    "dvla" %% "vehicles-presentation-common" % "2.26-SNAPSHOT" % "test" classifier "tests"  withSources() withJavadoc() exclude("junit", "junit-dep"),
    "org.webjars" % "webjars-play_2.10" % "2.3.0-3",
    "org.webjars" % "requirejs" % "2.1.16",
    "org.webjars" % "jquery" % "1.9.1",
    // Auditing service
    "com.rabbitmq" % "amqp-client" % "3.4.1",
    "junit" % "junit" % "4.11",
    "junit" % "junit-dep" % "4.11"
  )
}

val myTestOptions =
  if (System.getProperty("include") != null) {
    Seq(testOptions in Test += Tests.Argument("include", System.getProperty("include")))
  } else if (System.getProperty("exclude") != null) {
    Seq(testOptions in Test += Tests.Argument("exclude", System.getProperty("exclude")))
  } else Seq.empty[Def.Setting[_]]

myTestOptions

// If tests are annotated with @LiveTest then they are excluded when running sbt test
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-l", "helpers.tags.LiveTest")

javaOptions in Test += System.getProperty("waitSeconds")

concurrentRestrictions in Global := Seq(Tags.limit(Tags.CPU, 4), Tags.limit(Tags.Network, 10), Tags.limit(Tags.Test, 4))

sbt.Keys.fork in Test := false

// Using node to do the javascript optimisation cuts the time down dramatically
JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

// Disable documentation generation to save time for the CI build process
sources in doc in Compile := List()

ScalastylePlugin.Settings

net.virtualvoid.sbt.graph.Plugin.graphSettings

credentials += Credentials(Path.userHome / ".sbt/.credentials")

//////////////////
// Scoverage
//
// Code coverage plugin

ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;"

ScoverageKeys.coverageMinimum := 65

ScoverageKeys.coverageFailOnMinimum := true

ScoverageKeys.coverageHighlighting := true

// End Scoverage
//////////////////

resolvers ++= projectResolvers

// Uncomment before releasing to github in order to make Travis work
//resolvers ++= "Dvla Bintray Public" at "http://dl.bintray.com/dvla/maven/"

// ====================== Sandbox Settings ==========================
lazy val osAddressLookupProject = osAddressLookup("0.16-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)
lazy val vehicleAndKeeperLookupProject = vehicleAndKeeperLookup("0.15-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)
lazy val paymentSolveProject = paymentSolve("0.14-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)
lazy val vrmRetentionEligibilityProject = vrmRetentionEligibility("0.13-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)
lazy val vrmRetentionRetainProject = vrmRetentionRetain("0.13-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)
lazy val emailServiceProject = emailService("0.9-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)
//lazy val auditProject = audit("0.6-SNAPSHOT").disablePlugins(PlayScala, SbtWeb) // Disabled for now due to it needing to be in scala 2.11 but the webapp is still scala 2.10.
lazy val legacyStubsProject = legacyStubs("1.0-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)

SandboxSettings.portOffset := 18000

SandboxSettings.applicationContext := ""

SandboxSettings.webAppSecrets := "ui/dev/vrm-retention-online.conf.enc"

SandboxSettings.osAddressLookupProject := osAddressLookupProject

SandboxSettings.vehicleAndKeeperLookupProject := vehicleAndKeeperLookupProject

SandboxSettings.paymentSolveProject := paymentSolveProject

SandboxSettings.emailServiceProject := emailServiceProject

SandboxSettings.vrmRetentionEligibilityProject := vrmRetentionEligibilityProject

SandboxSettings.vrmRetentionRetainProject := vrmRetentionRetainProject

//SandboxSettings.auditProject := auditProject // Disabled for now due to it needing to be in scala 2.11 but the webapp is still scala 2.10.

SandboxSettings.legacyStubsProject := legacyStubsProject

SandboxSettings.runAllMicroservices := {
  Tasks.runLegacyStubs.value
  Tasks.runOsAddressLookup.value
  Tasks.runVehicleAndKeeperLookup.value
  Tasks.runPaymentSolve.value
  Tasks.runVrmRetentionEligibility.value
  Tasks.runVrmRetentionRetain.value
  Tasks.runEmailService.value
//  Tasks.runAudit.value // Disabled for now due to it needing to be in scala 2.11 but the webapp is still scala 2.10.
}

SandboxSettings.loadTests := (test in Gatling in gatlingTestsProject).value

SandboxSettings.acceptanceTests := (test in Test in acceptanceTestsProject).value

SandboxSettings.bruteForceEnabled:= true

Sandbox.sandboxTask

Sandbox.sandboxAsyncTask

Sandbox.gatlingTask

Sandbox.acceptTask

Sandbox.cucumberTask

Sandbox.acceptRemoteTask
