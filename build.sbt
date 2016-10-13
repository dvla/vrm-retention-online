import Common._
import com.typesafe.sbt.rjs.Import.RjsKeys.webJarCdns
import io.gatling.sbt.GatlingPlugin
import uk.gov.dvla.vehicles.sandbox.ProjectDefinitions.{audit, emailService, legacyStubs, osAddressLookup, paymentSolve}
import uk.gov.dvla.vehicles.sandbox.ProjectDefinitions.{vehicleAndKeeperLookup, vrmRetentionEligibility, vrmRetentionRetain}
import uk.gov.dvla.vehicles.sandbox.{Sandbox, SandboxSettings, Tasks}

name := "vrm-retention-online"

version := versionString

organization := organisationString

organizationName := organisationNameString

scalaVersion := scalaVersionString

scalacOptions := scalaOptionsSeq

publishTo <<= publishResolver

credentials += sbtCredentials

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtWeb)

lazy val acceptanceTestsProject = Project("acceptance-tests", file("acceptance-tests"))
  .dependsOn(root % "test->test")
  .disablePlugins(PlayScala, SbtWeb)

lazy val gatlingTestsProject = Project("gatling-tests", file("gatling-tests"))
  .disablePlugins(PlayScala, SbtWeb)
  .enablePlugins(GatlingPlugin)

libraryDependencies ++= Seq(
  filters,
  // Note that commons-collections transitive dependency of htmlunit has been excluded
  // We need to use version 3.2.2 of commons-collections to avoid the following in 3.2.1:
  // https://commons.apache.org/proper/commons-collections/security-reports.html#Apache_Commons_Collections_Security_Vulnerabilities
  "commons-collections" % "commons-collections" % "3.2.2" withSources() withJavadoc(),
  "commons-codec" % "commons-codec" % "1.10" withSources() withJavadoc(),
  // Auditing service
  "com.rabbitmq" % "amqp-client" % "3.4.1",
  "com.google.inject" % "guice" % "4.0" withSources() withJavadoc(),
  "com.google.guava" % "guava" % "19.0" withSources() withJavadoc(), // See: http://stackoverflow.com/questions/16614794/illegalstateexception-impossible-to-get-artifacts-when-data-has-not-been-loaded
  "com.sun.mail" % "javax.mail" % "1.5.2",
  "com.tzavellas" % "sse-guice" % "0.7.1" withSources() withJavadoc(), // Scala DSL for Guice
  "org.apache.pdfbox" % "pdfbox" % "1.8.6" withSources() withJavadoc(),
  "org.apache.pdfbox" % "preflight" % "1.8.6" withSources() withJavadoc(),
  "org.webjars" % "jquery" % "1.9.1",
  "org.webjars" % "requirejs" % "2.2.0",
  "org.webjars" %% "webjars-play" % "2.3.0-3",
  // test
  "org.seleniumhq.selenium" % "selenium-java" % "2.52.0" % "test",
  "com.codeborne" % "phantomjsdriver" % "1.2.1" % "test" withSources() withJavadoc(),
  "net.sourceforge.htmlunit" % "htmlunit" % "2.19" % "test" exclude("commons-collections", "commons-collections"),
  "com.github.tomakehurst" % "wiremock" % "1.58" % "test" withSources() withJavadoc() exclude("log4j", "log4j"),
  "info.cukes" % "cucumber-java" % "1.2.4" % "test" withSources() withJavadoc(),
  "junit" % "junit" % "4.11" % "test",
  "junit" % "junit-dep" % "4.11" % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test" withSources() withJavadoc(),
  "org.scalatest" %% "scalatest" % "2.2.6" % "test" withSources() withJavadoc(),
  "org.slf4j" % "log4j-over-slf4j" % "1.7.21" % "test" withSources() withJavadoc(),
  // VMPR
  "dvla" %% "vehicles-presentation-common" % "2.57-SNAPSHOT" withSources() withJavadoc() exclude("junit", "junit-dep"),
  "dvla" %% "vehicles-presentation-common" % "2.57-SNAPSHOT" % "test" classifier "tests" withSources() withJavadoc() exclude("junit", "junit-dep")
)

pipelineStages := Seq(rjs, digest, gzip)

val myTestOptions =
  if (System.getProperty("include") != null) {
    Seq(testOptions in Test += Tests.Argument("include", System.getProperty("include")))
  } else if (System.getProperty("exclude") != null) {
    Seq(testOptions in Test += Tests.Argument("exclude", System.getProperty("exclude")))
  } else Seq.empty[Def.Setting[_]]

myTestOptions

// use this to get a full stack trace when test failures occur
//testOptions in Test += Tests.Argument("-oUDF")

// If tests are annotated with @LiveTest then they are excluded when running sbt test
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-l", "helpers.tags.LiveTest")

javaOptions in Test += System.getProperty("waitSeconds")

// use this to get a full stack trace when test failures occur
//testOptions in Test := Seq(Tests.Filter(s => (s.endsWith("IntegrationSpec") || s.endsWith("UiSpec"))))

concurrentRestrictions in Global := Seq(Tags.limit(Tags.CPU, 4), Tags.limit(Tags.Network, 10), Tags.limit(Tags.Test, 4))

//parallelExecution in Test := true
//parallelExecution in Test in acceptanceTestsProject := true

sbt.Keys.fork in Test := false

// Using node to do the javascript optimisation cuts the time down dramatically
JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

// Disable documentation generation to save time for the CI build process
sources in doc in Compile := List()

credentials += Credentials(Path.userHome / ".sbt/.credentials")

// Scoverage - avoid play! framework generated classes
coverageExcludedPackages := "<empty>;Reverse.*"

coverageMinimum := 70

coverageFailOnMinimum := false

// highlighting will work as of scala 2.10.4 so no need to disable - see https://github.com/scala/scala/pull/3799
//coverageHighlighting := false

resolvers ++= projectResolvers

webJarCdns := Map()

// Uncomment before releasing to github in order to make Travis work
//resolvers ++= "Dvla Bintray Public" at "http://dl.bintray.com/dvla/maven/"

// ====================== Sandbox Settings ==========================
lazy val auditProject = audit("0.16-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)
lazy val emailServiceProject = emailService("0.22-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)
lazy val legacyStubsProject = legacyStubs("1.0-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)
lazy val osAddressLookupProject = osAddressLookup("0.31-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)
lazy val paymentSolveProject = paymentSolve("0.31-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)
lazy val vehicleAndKeeperLookupProject = vehicleAndKeeperLookup("0.27-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)
lazy val vrmRetentionEligibilityProject = vrmRetentionEligibility("0.24-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)
lazy val vrmRetentionRetainProject = vrmRetentionRetain("0.26-SNAPSHOT").disablePlugins(PlayScala, SbtWeb)

SandboxSettings.portOffset := 18000

SandboxSettings.applicationContext := ""

SandboxSettings.webAppSecrets := "vrm-retention-online/conf/vrm-retention-online.conf"

SandboxSettings.osAddressLookupProject := osAddressLookupProject

SandboxSettings.vehicleAndKeeperLookupProject := vehicleAndKeeperLookupProject

SandboxSettings.paymentSolveProject := paymentSolveProject

SandboxSettings.emailServiceProject := emailServiceProject

SandboxSettings.vrmRetentionEligibilityProject := vrmRetentionEligibilityProject

SandboxSettings.vrmRetentionRetainProject := vrmRetentionRetainProject

SandboxSettings.auditProject := auditProject

SandboxSettings.legacyStubsProject := legacyStubsProject

SandboxSettings.runAllMicroservices := {
  Tasks.runLegacyStubs.value
  Tasks.runOsAddressLookup.value
  Tasks.runVehicleAndKeeperLookup.value
  Tasks.runPaymentSolve.value
  Tasks.runVrmRetentionEligibility.value
  Tasks.runVrmRetentionRetain.value
  Tasks.runEmailService.value
  Tasks.runAudit.value
}

SandboxSettings.loadTests := (test in Gatling in gatlingTestsProject).value

SandboxSettings.acceptanceTests := (test in Test in acceptanceTestsProject).value

SandboxSettings.bruteForceEnabled := true

Sandbox.sandboxTask

Sandbox.sandboxAsyncTask

Sandbox.gatlingTask

Sandbox.acceptTask

Sandbox.cucumberTask

Sandbox.acceptRemoteTask
