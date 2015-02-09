// Comment to get more information during initialization
logLevel := Level.Debug

// Our plugin resolvers
resolvers += "Nexus snapshots" at "http://rep002-01.skyscape.preview-dvla.co.uk:8081/nexus/content/repositories/snapshots"

resolvers += "Nexus releases" at "http://rep002-01.skyscape.preview-dvla.co.uk:8081/nexus/content/repositories/releases"

addSbtPlugin("dvla" % "build-details-generator" % "1.3.1")

addSbtPlugin("dvla" % "microservices-sandbox" % "1.3.2")

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"

resolvers += "Maven 2" at "http://repo2.maven.org/maven2"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

addSbtPlugin("net.litola" % "play-sass" % "0.4.0")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.4.0")

resolvers += "Nexus Repository" at "http://rep002-01.skyscape.preview-dvla.co.uk:8081/nexus/content/repositories/thirdparty/"

resolvers += "Templemore Repository" at "http://templemore.co.uk/repo/"

addSbtPlugin("templemore" % "sbt-cucumber-plugin" % "0.8.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

// Plugin for publishing scoverage results to coveralls
addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "0.99.10.2")

addSbtPlugin("org.scoverage" %% "sbt-coveralls" % "0.99.0")

addSbtPlugin("io.gatling" % "gatling-sbt" % "2.1.0")
