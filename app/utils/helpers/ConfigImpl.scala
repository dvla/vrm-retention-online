package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.booleanProp
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getOptionalProperty
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getProperty
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getStringListProperty
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.intProp
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.longProp
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.stringProp
import uk.gov.dvla.vehicles.presentation.common.services.SEND.EmailConfiguration
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.From

import scala.concurrent.duration.DurationInt

class ConfigImpl extends Config {

  val assetsUrl: Option[String] = getOptionalProperty[String]("assets.url")

  // Payment Service
  // TODO: this should not be optional
  override val purchaseAmountInPence: String =
    getOptionalProperty[String]("retention.purchaseAmountInPence").getOrElse("NOT FOUND")

  override val secureCookies = getOptionalProperty[Boolean]("secureCookies").getOrElse(true)

  override val encryptCookies = getOptionalProperty[Boolean]("encryptCookies").getOrElse(true)

  override val applicationCode: String = getOptionalProperty[String]("webHeader.applicationCode").getOrElse("NOT FOUND")

  override val channelCode: String = getOptionalProperty[String]("webHeader.channelCode").getOrElse("NOT FOUND")

  override val contactId: Long = getOptionalProperty[Long]("webHeader.contactId").getOrElse(0L)

  override val orgBusinessUnit: String = getOptionalProperty[String]("webHeader.orgBusinessUnit").getOrElse("NOT FOUND")

  override val vssServiceTypeCode: String =
    getOptionalProperty[String]("webHeader.vssServiceTypeCode").getOrElse("NOT FOUND")
  override val dmsServiceTypeCode: String =
    getOptionalProperty[String]("webHeader.dmsServiceTypeCode").getOrElse("NOT FOUND")

  override val vrmRetentionEligibilityMicroServiceUrlBase: String =
    getOptionalProperty[String]("vrmRetentionEligibilityMicroServiceUrlBase").getOrElse("NOT FOUND")

  override val vrmRetentionRetainMicroServiceUrlBase: String =
    getOptionalProperty[String]("vrmRetentionRetainMicroServiceUrlBase").getOrElse("NOT FOUND")

  override val vehicleAndKeeperLookupMicroServiceBaseUrl: String =
    getOptionalProperty[String]("vehicleAndKeeperLookupMicroServiceUrlBase").getOrElse("NOT FOUND")

  override val vrmRetentionEligibilityMsRequestTimeout: Int =
    getOptionalProperty[Int]("vrmRetentionEligibility.requesttimeout").getOrElse(30.seconds.toMillis.toInt)

  override val vrmRetentionRetainMsRequestTimeout: Int =
    getOptionalProperty[Int]("vrmRetentionRetain.requesttimeout").getOrElse(30.seconds.toMillis.toInt)

  override val paymentSolveMicroServiceUrlBase: String =
    getOptionalProperty[String]("paymentSolveMicroServiceUrlBase").getOrElse("NOT FOUND")

  override val paymentSolveMsRequestTimeout: Int =
    getOptionalProperty[Int]("paymentSolve.ms.requesttimeout").getOrElse(5.seconds.toMillis.toInt)

  override val vehicleAndKeeperLookupRequestTimeout: Int =
    getOptionalProperty[Int]("vehicleAndKeeperLookup.requesttimeout").getOrElse(30.seconds.toMillis.toInt)

  override val isPrototypeBannerVisible: Boolean = getOptionalProperty[Boolean]("prototype.disclaimer").getOrElse(true)

  override val googleAnalyticsTrackingId: Option[String] = getOptionalProperty[String]("googleAnalytics.id.retention")

  override val emailWhitelist: Option[List[String]] = getStringListProperty("email.whitelist")

  override val emailSenderAddress: String = getOptionalProperty[String]("email.senderAddress").getOrElse("")

  override val cookieMaxAge = getOptionalProperty[Int]("application.cookieMaxAge").getOrElse(30.minutes.toSeconds.toInt)

  override val storeBusinessDetailsMaxAge =
    getOptionalProperty[Int]("storeBusinessDetails.cookieMaxAge").getOrElse(7.days.toSeconds.toInt)

  override val auditMicroServiceUrlBase: String =
    getOptionalProperty[String]("auditMicroServiceUrlBase").getOrElse("NOT FOUND")

  override val auditMsRequestTimeout: Int =
    getOptionalProperty[Int]("audit.requesttimeout").getOrElse(30.seconds.toMillis.toInt)

  // Email microservice
  override val emailServiceMicroServiceUrlBase: String =
    getOptionalProperty[String]("emailServiceMicroServiceUrlBase").getOrElse("NOT FOUND")
  override val emailServiceMsRequestTimeout: Int =
    getOptionalProperty[Int]("emailService.ms.requesttimeout").getOrElse(30.seconds.toMillis.toInt)
  override val emailConfiguration: EmailConfiguration = EmailConfiguration(
    From(getProperty[String]("email.senderAddress"), "DO NOT REPLY"),
    From(getProperty[String]("email.feedbackAddress"), "Feedback"),
    getStringListProperty("email.whitelist")
  )

  override val openingTimeMinOfDay: Int = getProperty[Int]("openingTimeMinOfDay")
  override val closingTimeMinOfDay: Int = getProperty[Int]("closingTimeMinOfDay")
  override val closingWarnPeriodMins: Int = getOptionalProperty[Int]("closingWarnPeriodMins").getOrElse(15)

  override val surveyUrl: Option[String] = getOptionalProperty[String]("survey.url")
}