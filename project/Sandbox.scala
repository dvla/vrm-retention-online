import java.io.StringReader
import java.net.URLClassLoader
import com.typesafe.config.ConfigFactory
import org.apache.commons.io.FileUtils
import sbt.Keys._
import sbt._
import scala.sys.process.Process

object CommonResolvers {
  val nexus = "http://rep002-01.skyscape.preview-dvla.co.uk:8081/nexus/content/repositories"

  val projectResolvers = Seq(
    "typesafe repo" at "http://repo.typesafe.com/typesafe/releases",
    "spray repo" at "http://repo.spray.io/",
    "local nexus snapshots" at s"$nexus/snapshots",
    "local nexus releases" at s"$nexus/releases"
  )
}

object Sandbox extends Plugin {
  final val VersionOsAddressLookup = "0.6"
  final val VersionVehiclesLookup = "0.5"
  final val VersionVehicleAndKeeperLookup = "0.2"
  final val VersionVrmRetentionEligibility = "0.4"
  final val VersionVrmRetentionRetain = "0.3"
  final val VersionPaymentSolve= "0.3"
  final val VersionLegacyStubs = "1.0-SNAPSHOT"
  final val VersionJetty = "9.2.1.v20140609"
  final val VersionSpringWeb = "3.0.7.RELEASE"
  final val VersionVehiclesGatling = "1.0-SNAPSHOT"
  final val VersionGatling = "1.0-SNAPSHOT"
  final val VersionGatlingApp = "2.0.0-M4-NAP"

  final val HttpsPort = 18443
  final val OsAddressLookupPort = 18801
  final val VehicleLookupPort = 18802
  final val VehicleAndKeeperLookupPort = 18803
  final val VrmRetentionEligibilityPort = 18804
  final val VrmRetentionRetainPort = 18805
  final val LegacyServicesStubsPort = 18806
  final val PaymentSolvePort = 18807

  val secretProperty = "DECRYPT_PASSWORD"
  val secretProperty2 = "GIT_SECRET_PASSPHRASE"
  val SecretRepoGitUrlKey = "SANDBOX_SECRET_REPO_GIT_URL"
  val secretRepoUrl = sys.props.get(SecretRepoGitUrlKey)
    .orElse(sys.env.get(SecretRepoGitUrlKey))
  val gitHost = secretRepoUrl.map(url=> url.replace("git@", "").substring(0, url.indexOf(":") - 4))

  val decryptPassword = sys.props.get(secretProperty)
    .orElse(sys.env.get(secretProperty))
    .orElse(sys.props.get(secretProperty2))
    .orElse(sys.env.get(secretProperty2))

  def sandProject(name: String, deps: ModuleID*): (Project, ScopeFilter) =
    sandProject(name,  Seq[Resolver](), deps: _*)

  def sandProject(name: String,
                  res: Seq[Resolver],
                  deps: ModuleID*): (Project, ScopeFilter) = (
    Project(name, file(s"target/sandbox/$name"))
      .settings(libraryDependencies ++= deps)
      .settings(resolvers ++= (CommonResolvers.projectResolvers ++ res))
      .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*),
    ScopeFilter(inProjects(LocalProject(name)), inConfigurations(Runtime))
  )

  lazy val (osAddressLookup, scopeOsAddressLookup) =
    sandProject("os-address-lookup","dvla" %% "os-address-lookup" % VersionOsAddressLookup)
  lazy val (vehiclesLookup, scopeVehiclesLookup) =
    sandProject("vehicles-lookup", "dvla" %% "vehicles-lookup" % VersionVehiclesLookup)
  lazy val (vehicleAndKeeperLookup, scopeVehicleAndKeeperLookup) =
    sandProject("vehicle-and-keeper-lookup", "dvla" %% "vehicle-and-keeper-lookup" % VersionVehicleAndKeeperLookup)
  lazy val (vrmRetentionEligibility, scopeVrmRetentionEligibility) =
    sandProject("vrm-retention-eligibility", "dvla" %% "vrm-retention-eligibility" % VersionVrmRetentionEligibility)
  lazy val (vrmRetentionRetain, scopeVrmRetentionRetain) =
    sandProject("vrm-retention-retain", "dvla" %% "vrm-retention-retain" % VersionVrmRetentionRetain)
  lazy val (paymentSolve, scopePaymentSolve) =
    sandProject("payment-solve", "dvla" %% "payment-solve" % VersionPaymentSolve)
  lazy val (legacyStubs, scopeLegacyStubs) = sandProject(
    name = "legacy-stubs",
    "dvla-legacy-stub-services" % "legacy-stub-services-service" % VersionLegacyStubs,
    "org.eclipse.jetty" % "jetty-server" % VersionJetty,
    "org.eclipse.jetty" % "jetty-servlet" % VersionJetty,
    "org.springframework" % "spring-web" % VersionSpringWeb
  )
  lazy val (gatlingTests, scopeGatlingTests) = sandProject (
    name = "gatling",
    Seq("Central Maven" at "http://central.maven.org/maven2"),
    "com.netaporter.gatling" % "gatling-app" % VersionGatlingApp,
    "uk.gov.dvla" % "vehicles-gatling" % VersionVehiclesGatling
  )

//  lazy val sandboxedProjects = Seq(osAddressLookup, vehiclesLookup, vehicleAndKeeperLookup, legacyStubs)

  lazy val vehiclesOnline = ScopeFilter(inProjects(ThisProject), inConfigurations(Runtime))

  lazy val runMicroServices = taskKey[Unit]("Runs all the required by the sandbox micro services'")
  lazy val runMicroServicesTask = runMicroServices := {
    validatePrerequisites()

    val targetFolder = (target in ThisProject).value
    val secretRepoFolder = new File(targetFolder, "secretRepo")
    updateSecretVehiclesOnline(secretRepoFolder)

    runProject(
      fullClasspath.all(scopeOsAddressLookup).value.flatten,
      Some(ConfigDetails(
        secretRepoFolder,
        "ms/dev/os-address-lookup.conf.enc",
        Some(ConfigOutput(
          new File(classDirectory.all(scopeOsAddressLookup).value.head, s"${osAddressLookup.id}.conf"),
          setServicePort(OsAddressLookupPort)
        ))
      ))
    )
    runProject(
      fullClasspath.all(scopeVehiclesLookup).value.flatten,
      Some(ConfigDetails(
        secretRepoFolder,
        "ms/dev/vehicles-lookup.conf.enc",
        Some(ConfigOutput(
          new File(classDirectory.all(scopeVehiclesLookup).value.head, s"${vehiclesLookup.id}.conf"),
          setServicePortAndLegacyServicesPort(VehicleLookupPort, "getVehicleDetails.baseurl", LegacyServicesStubsPort)
        ))
      ))
    )
    runProject(
      fullClasspath.all(scopeVehicleAndKeeperLookup).value.flatten,
      Some(ConfigDetails(
        secretRepoFolder,
        "ms/dev/vehicle-and-keeper-lookup.conf.enc",
        Some(ConfigOutput(
          new File(classDirectory.all(scopeVehicleAndKeeperLookup).value.head, s"${vehicleAndKeeperLookup.id}.conf"),
          setServicePortAndLegacyServicesPort(
            VehicleAndKeeperLookupPort,
            "getVehicleAndKeeperDetails.baseurl",
            LegacyServicesStubsPort
          )
        ))
      ))
    )
    runProject(
      fullClasspath.all(scopeVrmRetentionEligibility).value.flatten,
      Some(ConfigDetails(
        secretRepoFolder,
        "ms/dev/vrm-retention-eligibility.conf.enc",
        Some(ConfigOutput(
          new File(classDirectory.all(scopeVrmRetentionEligibility).value.head, s"${vrmRetentionEligibility.id}.conf"),
          setServicePortAndLegacyServicesPort(
            VrmRetentionEligibilityPort,
            "validateRetain.url",
            LegacyServicesStubsPort
          )
        ))
      ))
    )
    runProject(
      fullClasspath.all(scopeVrmRetentionRetain).value.flatten,
      Some(ConfigDetails(
        secretRepoFolder,
        "ms/dev/vrm-retention-retain.conf.enc",
        Some(ConfigOutput(
          new File(classDirectory.all(scopeVrmRetentionRetain).value.head, s"${vrmRetentionRetain.id}.conf"),
          setServicePortAndLegacyServicesPort(VrmRetentionRetainPort, "retain.url", LegacyServicesStubsPort)
        ))
      ))
    )
    runProject(
      fullClasspath.all(scopePaymentSolve).value.flatten,
      Some(ConfigDetails(
        secretRepoFolder,
        "ms/dev/payment-solve.conf.enc",
        Some(ConfigOutput(
          new File(classDirectory.all(scopePaymentSolve).value.head, s"${paymentSolve.id}.conf"),
          setServicePort(PaymentSolvePort)
        ))
      ))
    )
    runProject(
      fullClasspath.all(scopeLegacyStubs).value.flatten,
      None,
      runJavaMain("service.LegacyServicesRunner", Array(LegacyServicesStubsPort.toString))
    )
  }

  lazy val sandbox = taskKey[Unit]("Runs the whole sandbox for manual testing including microservices, webapp and legacy stubs'")
  lazy val sandboxTask = sandbox <<= (runMicroServices, (run in Runtime).toTask("")) { (body, stop) =>
    System.setProperty("vehicleLookupMicroServiceUrlBase", s"http://localhost:$VehicleLookupPort")
    System.setProperty("vehicleAndKeeperLookupMicroServiceUrlBase", s"http://localhost:$VehicleAndKeeperLookupPort")
    System.setProperty("vrmRetentionEligibilityMicroServiceUrlBase", s"http://localhost:$VrmRetentionEligibilityPort")
    System.setProperty("vrmRetentionRetainMicroServiceUrlBase", s"http://localhost:$VrmRetentionRetainPort")
    System.setProperty("paymentSolveMicroServiceUrlBase", s"http://localhost:$PaymentSolvePort")
    System.setProperty("ordnancesurvey.baseUrl", s"http://localhost:$OsAddressLookupPort")
    body.flatMap(t => stop)
  }

  lazy val testGatling = taskKey[Unit]("Runs the gatling test")
  lazy val testGatlingTask = testGatling := {
    val classPath = fullClasspath.all(scopeGatlingTests).value.flatten
    val targetFolder = target.in(gatlingTests).value.getAbsolutePath
    val vehiclesGatlingExtractDir = new File(s"$targetFolder/gatlingJarExtract")

    def extractVehiclesGatlingJar(toFolder: File) =
      classPath.find(_.data.toURI.toURL.toString.endsWith(s"vehicles-gatling-$VersionVehiclesGatling.jar"))
        .map { jar => IO.unzip(new File(jar.data.toURI.toURL.getFile), toFolder)}

    def simulation(arg: String): Unit = runProject(
      classPath,
      None,
      runJavaMain(
        mainClassName = "io.gatling.app.Gatling",
        args = Array(
          "--simulation", arg,
          "--data-folder", s"${vehiclesGatlingExtractDir.getAbsolutePath}/data",
          "--results-folder", s"$targetFolder/gatling",
          "--request-bodies-folder", s"$targetFolder/request-bodies"
        ),
        method = "runGatling"
      )
    ) match {
      case 0 => println("Gatling execution SUCCESS")
      case exitCode =>
        println("Gatling execution FAILURE")
        throw new Exception(s"Gatling run exited with error $exitCode")
    }

    IO.delete(vehiclesGatlingExtractDir)
    vehiclesGatlingExtractDir.mkdirs()
    extractVehiclesGatlingJar(vehiclesGatlingExtractDir)
    System.setProperty("gatling.core.disableCompiler", "true")

    simulation("uk.gov.dvla.retention.Simulate")
  }

  lazy val runAsync = taskKey[Unit]("Runs the play application")
  lazy val runAsyncTask = runAsync := {
    System.setProperty("https.port", HttpsPort.toString)
    System.setProperty("http.port", "disabled")
    System.setProperty("baseUrl", s"https://localhost:$HttpsPort")
    runProject(
      fullClasspath.in(Test).value,
      None,
      runScalaMain("play.core.server.NettyServer", Array((baseDirectory in ThisProject).value.getAbsolutePath))
    )
  }

  lazy val sandboxAsync = taskKey[Unit]("Runs the whole sandbox asynchronously for manual testing including microservices, webapp and legacy stubs")
  lazy val sandboxAsyncTask = sandboxAsync <<= (runMicroServices, (runAsync in Runtime).toTask) { (body, stop) =>
    body.flatMap(t => stop)
  }

  lazy val gatling = taskKey[Unit]("Runs the gatling tests against the sandbox")
  lazy val gatlingTask = gatling <<= (sandboxAsync, (testGatling in Runtime).toTask) { (body, stop) =>
    body.flatMap(t => stop)
  }

  lazy val sandboxSettings = Seq(
    runMicroServicesTask,
    sandboxTask,
    runAsyncTask,
    testGatlingTask,
    sandboxAsyncTask,
    gatlingTask
  )


  def validatePrerequisites() {
    print(s"${scala.Console.YELLOW}Verifying git is installed...${scala.Console.RESET}")
    if (Process("git --version").! != 0) {
      println(s"${scala.Console.RED}FAILED.")
      println(s"You don't have git installed. Please install git and try again${scala.Console.RESET}")
      throw new Exception("You don't have git installed. Please install git and try again")
    }

    print(s"${scala.Console.YELLOW}Verifying $SecretRepoGitUrlKey is passed ...${scala.Console.RESET}")
    secretRepoUrl map(secret => println(s"done set to $secret")) orElse {
      println(s"""${scala.Console.RED}FAILED.${scala.Console.RESET}""")
      println(s"""${scala.Console.RED}"$SecretRepoGitUrlKey" not set. Please set it either as jvm arg of sbt """ +
        s""" "-D$SecretRepoGitUrlKey='git@git-host:theSecretRepoProjectName'"""" +
        s" or export it in the environment with export $SecretRepoGitUrlKey='git@git-host:theSecretRepoProjectName'" +
        s" ${scala.Console.RESET}")
      throw new Exception(s""" There is no "$SecretRepoGitUrlKey" set neither as env variable nor as JVM property """)
    }

    print(s"${scala.Console.YELLOW}Verifying there is ssh access to ${gitHost.get} ...${scala.Console.RESET}")
    if (Process(s"ssh -T git@${gitHost.get}").! != 0) {
      println(s"${scala.Console.RED}FAILED.")
      println(s"Cannot connect to git@${gitHost.get}. Please check your ssh connection to ${gitHost.get}. You might need to import your public key to $gitHost${scala.Console.RESET}")
      throw new Exception(s"Cannot connect to git@${gitHost.get}. Please check your ssh connection to ${gitHost.get}.")
    }

    print(s"${scala.Console.YELLOW}Verifying $secretProperty is passed ...${scala.Console.RESET}")
    decryptPassword map(secret => println("done")) orElse {
      println(s"""${scala.Console.RED}FAILED.${scala.Console.RESET}""")
      println(s"""${scala.Console.RED}"$secretProperty" not set. Please set it either as jvm arg of sbt """ +
        s""" "-D$secretProperty='secret'"""" +
        s" or export it in the environment with export $secretProperty='some secret prop' ${scala.Console.RESET}")
      throw new Exception(s""" There is no "$secretProperty" set neither as env variable nor as JVM property """)
    }
  }

  def withClassLoader[T](classLoader: ClassLoader)(code: => T) {
    val currentContextClassLoader = Thread.currentThread().getContextClassLoader
    Thread.currentThread().setContextClassLoader(classLoader)
    try code
    finally Thread.currentThread().setContextClassLoader(currentContextClassLoader)
  }

  def runScalaMain(mainClassName: String, args: Array[String] = Array[String](), method: String = "main")
                  (prjClassLoader: ClassLoader): Any = withClassLoader[Any](prjClassLoader) {
    import scala.reflect.runtime.universe.{newTermName, runtimeMirror}
    lazy val mirror = runtimeMirror(prjClassLoader)
    val bootSymbol = mirror.staticModule(mainClassName).asModule
    val boot = mirror.reflectModule(bootSymbol).instance
    val mainMethodSymbol = bootSymbol.typeSignature.member(newTermName(method)).asMethod
    val bootMirror = mirror.reflect(boot)
    bootMirror.reflectMethod(mainMethodSymbol).apply(args)
  }

  def runJavaMain(mainClassName: String, args: Array[String] = Array[String](), method: String = "main")
                 (prjClassLoader: ClassLoader): Any = withClassLoader(prjClassLoader) {
    val mainClass = prjClassLoader.loadClass(mainClassName)
    val mainMethod = mainClass.getMethod(method, classOf[Array[String]])
    val mainResult = mainMethod.invoke(null, args)
    return mainResult
  }

  case class ConfigDetails(secretRepo: File,
                          encryptedConfig: String,
                          output: Option[ConfigOutput],
                          systemPropertySetter: String => Unit = a => ())

  case class ConfigOutput(decryptedOutput: File, transform: String => String = a => a)

  def runProject(prjClassPath: Seq[Attributed[File]],
                 configDetails: Option[ConfigDetails],
                 runMainMethod: (ClassLoader) => Any = runScalaMain("dvla.microservice.Boot")): Any = {
    configDetails.map { case ConfigDetails(secretRepo, encryptedConfig, output, systemPropertySetter) =>
      val encryptedConfigFile = new File(secretRepo, encryptedConfig)
      output.map { case ConfigOutput(decryptedOutput, transform)=>
        decryptFile(secretRepo.getAbsolutePath, encryptedConfigFile, decryptedOutput, transform)
      }
    }

    val prjClassloader = new URLClassLoader(
      prjClassPath.map(_.data.toURI.toURL).toArray,
      getClass.getClassLoader.getParent.getParent
    )

    runMainMethod(prjClassloader)
  }

  def updateSecretVehiclesOnline(secretRepo: File) {
    val secretRepoLocalPath = secretRepo.getAbsolutePath
    val gitOptions = s"--work-tree $secretRepoLocalPath --git-dir $secretRepoLocalPath/.git"


    if (new File(secretRepo, ".git").exists())
      println(Process(s"git $gitOptions pull origin master").!!<)
    else
      println(Process(s"git clone ${secretRepoUrl.get} $secretRepoLocalPath").!!<)
  }

  def decryptFile(secretRepo: String, encrypted: File, dest: File, decryptedTransform: String => String) {
    val decryptFile = s"$secretRepo/decrypt-file"
    dest.getParentFile.mkdirs()
    val decryptCommand = s"$decryptFile ${encrypted.getAbsolutePath} ${dest.getAbsolutePath} ${decryptPassword.get}"
    Process(decryptCommand).!!<

    val transformedFile = decryptedTransform(FileUtils.readFileToString(dest))
    FileUtils.writeStringToFile(dest, transformedFile)
  }

  def setServicePortAndLegacyServicesPort(servicePort: Int, urlProperty: String, newPort: Int)
                                         (properties: String): String =
    setServicePort(servicePort)(updatePropertyPort(urlProperty, newPort)(properties))

  def setServicePort(servicePort: Int)(properties: String): String = {
    s"""
    |$properties
    |port=$servicePort
    """.stripMargin
  }

  def updatePropertyPort(urlProperty: String, newPort: Int)(properties: String): String = {
    val config = ConfigFactory.parseReader(new StringReader(properties))
    val url = new URL(config.getString(urlProperty))

    val newUrl = new URL(url.getProtocol, url.getHost, newPort, url.getFile).toString

    properties.replace(url.toString, newUrl.toString)
  }
}
