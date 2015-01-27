package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._

import scala.concurrent.duration.DurationInt

class Config {


  // Prototype message in html
  lazy val isPrototypeBannerVisible: Boolean = getOptionalProperty[Boolean]("prototype.disclaimer").getOrElse(true)

  // Prototype survey URL
  lazy val prototypeSurveyUrl: String = getOptionalProperty[String]("survey.url").getOrElse("")
  lazy val prototypeSurveyPrepositionInterval: Long = getOptionalProperty[Long]("survey.interval").getOrElse(7.days.toMillis)

  // Google analytics
  lazy val googleAnalyticsTrackingId: Option[String] = getOptionalProperty[String]("googleAnalytics.id.retention")

  // Progress step indicator
  lazy val isProgressBarEnabled: Boolean = getOptionalProperty[Boolean]("progressBar.enabled").getOrElse(true)

  // Rabbit-MQ
  lazy val rabbitmqHost = getOptionalProperty[String]("rabbitmq.host").getOrElse("NOT FOUND")
  lazy val rabbitmqPort = getOptionalProperty[Int]("rabbitmq.port").getOrElse(0)
  lazy val rabbitmqQueue = getOptionalProperty[String]("rabbitmq.queue").getOrElse("NOT FOUND")
  lazy val rabbitmqUsername = getOptionalProperty[String]("rabbitmq.username").getOrElse("NOT FOUND")
  lazy val rabbitmqPassword = getOptionalProperty[String]("rabbitmq.password").getOrElse("NOT FOUND")
  lazy val rabbitmqVirtualHost = getOptionalProperty[String]("rabbitmq.virtualHost").getOrElse("NOT FOUND")

  // Payment Service
  lazy val renewalFee: String = getOptionalProperty[String]("assign.renewalFee").getOrElse("NOT FOUND")

  // Email Service
  lazy val emailSmtpHost: String = getOptionalProperty[String]("smtp.host").getOrElse("")
  lazy val emailSmtpPort: Int = getOptionalProperty[Int]("smtp.port").getOrElse(25)
  lazy val emailSmtpSsl: Boolean = getOptionalProperty[Boolean]("smtp.ssl").getOrElse(false)
  lazy val emailSmtpTls: Boolean = getOptionalProperty[Boolean]("smtp.tls").getOrElse(true)
  lazy val emailSmtpUser: String = getOptionalProperty[String]("smtp.user").getOrElse("")
  lazy val emailSmtpPassword: String = getOptionalProperty[String]("smtp.password").getOrElse("")
  lazy val emailWhitelist: Option[List[String]] = getStringListProperty("email.whitelist")
  //getOptionalProperty[("email.whitelist", "").split(",")
  lazy val emailSenderAddress: String = getOptionalProperty[String]("email.senderAddress").getOrElse("")

  lazy val cookieMaxAge = getOptionalProperty[Int]("application.cookieMaxAge").getOrElse(30.minutes.toSeconds.toInt)
  lazy val storeBusinessDetailsMaxAge = getOptionalProperty[Int]("storeBusinessDetails.cookieMaxAge").getOrElse(7.days.toSeconds.toInt)

  // Audir microservice
  val auditMicroServiceUrlBase: String = getOptionalProperty[String]("auditMicroServiceUrlBase").getOrElse("NOT FOUND")
  val auditMsRequestTimeout: Int = getOptionalProperty[Int]("audit.requesttimeout").getOrElse(30.seconds.toMillis.toInt)

  // Web headers
  val orgBusinessUnit: String = getOptionalProperty[String]("webHeader.orgBusinessUnit").getOrElse("NOT FOUND")
}