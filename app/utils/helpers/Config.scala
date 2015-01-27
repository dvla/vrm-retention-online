package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._

import scala.concurrent.duration.DurationInt

class Config {

  // Email Service
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