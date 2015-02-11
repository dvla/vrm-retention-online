package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._

import scala.concurrent.duration.DurationInt

class ConfigImpl extends Config {

  // Payment Service
  override def purchaseAmount: String = getOptionalProperty[String]("retention.purchaseAmountInPence").getOrElse("NOT FOUND")

  override def secureCookies = getOptionalProperty[Boolean]("secureCookies").getOrElse(true)

  override def encryptCookies = getOptionalProperty[Boolean]("encryptCookies").getOrElse(true)

  override def ordnanceSurveyUseUprn: Boolean = getOptionalProperty[Boolean]("ordnancesurvey.useUprn").getOrElse(false)

  override def applicationCode: String = getOptionalProperty[String]("webHeader.applicationCode").getOrElse("NOT FOUND")

  override def channelCode: String = getOptionalProperty[String]("webHeader.channelCode").getOrElse("NOT FOUND")

  override def contactId: Long = getOptionalProperty[Long]("webHeader.contactId").getOrElse(0L)

  override def orgBusinessUnit: String = getOptionalProperty[String]("webHeader.orgBusinessUnit").getOrElse("NOT FOUND")

  override def vssServiceTypeCode: String = getOptionalProperty[String]("webHeader.vssServiceTypeCode").getOrElse("NOT FOUND")
  override def dmsServiceTypeCode: String = getOptionalProperty[String]("webHeader.dmsServiceTypeCode").getOrElse("NOT FOUND")

  override def vrmRetentionEligibilityMicroServiceUrlBase: String = getOptionalProperty[String]("vrmRetentionEligibilityMicroServiceUrlBase").getOrElse("NOT FOUND")

  override def vrmRetentionRetainMicroServiceUrlBase: String = getOptionalProperty[String]("vrmRetentionRetainMicroServiceUrlBase").getOrElse("NOT FOUND")

  override def vehicleAndKeeperLookupMicroServiceBaseUrl: String = getOptionalProperty[String]("vehicleAndKeeperLookupMicroServiceUrlBase").getOrElse("NOT FOUND")

  override def vrmRetentionEligibilityMsRequestTimeout: Int = getOptionalProperty[Int]("vrmRetentionEligibility.requesttimeout").getOrElse(30.seconds.toMillis.toInt)

  override def vrmRetentionRetainMsRequestTimeout: Int = getOptionalProperty[Int]("vrmRetentionRetain.requesttimeout").getOrElse(30.seconds.toMillis.toInt)

  override def paymentSolveMicroServiceUrlBase: String = getOptionalProperty[String]("paymentSolveMicroServiceUrlBase").getOrElse("NOT FOUND")

  override def paymentSolveMsRequestTimeout: Int = getOptionalProperty[Int]("paymentSolve.ms.requesttimeout").getOrElse(5.seconds.toMillis.toInt)

  override def vehicleAndKeeperLookupRequestTimeout: Int = getOptionalProperty[Int]("vehicleAndKeeperLookup.requesttimeout").getOrElse(30.seconds.toMillis.toInt)

  override def isPrototypeBannerVisible: Boolean = getOptionalProperty[Boolean]("prototype.disclaimer").getOrElse(true)

  override def googleAnalyticsTrackingId: Option[String] = getOptionalProperty[String]("googleAnalytics.id.retention")

  override def isProgressBarEnabled: Boolean = getOptionalProperty[Boolean]("progressBar.enabled").getOrElse(true)

  // Rabbit-MQ
  override def rabbitmqHost = getOptionalProperty[String]("rabbitmq.host").getOrElse("NOT FOUND")

  override def rabbitmqPort = getOptionalProperty[Int]("rabbitmq.port").getOrElse(0)

  override def rabbitmqQueue = getOptionalProperty[String]("rabbitmq.queue").getOrElse("NOT FOUND")

  override def rabbitmqUsername = getOptionalProperty[String]("rabbitmq.username").getOrElse("NOT FOUND")

  override def rabbitmqPassword = getOptionalProperty[String]("rabbitmq.password").getOrElse("NOT FOUND")

  override def rabbitmqVirtualHost = getOptionalProperty[String]("rabbitmq.virtualHost").getOrElse("NOT FOUND")

  override def emailWhitelist: Option[List[String]] = getStringListProperty("email.whitelist")

  override def emailSenderAddress: String = getOptionalProperty[String]("email.senderAddress").getOrElse("")

  override def cookieMaxAge = getOptionalProperty[Int]("application.cookieMaxAge").getOrElse(30.minutes.toSeconds.toInt)

  override def storeBusinessDetailsMaxAge = getOptionalProperty[Int]("storeBusinessDetails.cookieMaxAge").getOrElse(7.days.toSeconds.toInt)

  override def auditMicroServiceUrlBase: String = getOptionalProperty[String]("auditMicroServiceUrlBase").getOrElse("NOT FOUND")

  override def auditMsRequestTimeout: Int = getOptionalProperty[Int]("audit.requesttimeout").getOrElse(30.seconds.toMillis.toInt)

  // Email microservice
  override val emailServiceMicroServiceUrlBase: String = getOptionalProperty[String]("emailServiceMicroServiceUrlBase").getOrElse("NOT FOUND")
  override val emailServiceMsRequestTimeout: Int = getOptionalProperty[Int]("emailService.ms.requesttimeout").getOrElse(30.seconds.toMillis.toInt)

}