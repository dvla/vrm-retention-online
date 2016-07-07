package utils.helpers

import play.api.Logger
import uk.gov.dvla.vehicles.presentation.{common => VPC}
import VPC.ConfigProperties.booleanProp
import VPC.ConfigProperties.getOptionalProperty
import VPC.ConfigProperties.getProperty
import VPC.ConfigProperties.getStringListProperty
import VPC.ConfigProperties.getIntListProperty
import VPC.ConfigProperties.intProp
import VPC.ConfigProperties.longProp
import VPC.ConfigProperties.stringProp
import VPC.services.SEND.EmailConfiguration
import VPC.webserviceclients.emailservice.From

import scala.concurrent.duration.DurationInt

final class ConfigImpl extends Config {

  val assetsUrl: Option[String] = getOptionalProperty[String]("assets.url")

  // Payment Service
  override val purchaseAmountInPence: String =
    getProperty[String]("retention.purchaseAmountInPence")

  override val secureCookies = getOptionalProperty[Boolean]("secureCookies").getOrElse(ConfigImpl.DEFAULT_SECURECOOKIES)

  override val encryptCookies = getOptionalProperty[Boolean]("encryptCookies").getOrElse(ConfigImpl.DEFAULT_ENCRYPTEDCOOKIES)

  override val applicationCode: String = getOptionalProperty[String]("webHeader.applicationCode").getOrElse(ConfigImpl.DEFAULT_WH_APPLICATION_CODE)

  override val channelCode: String = getOptionalProperty[String]("webHeader.channelCode").getOrElse(ConfigImpl.DEFAULT_WH_CHANNEL_CODE)

  override val contactId: Long = getOptionalProperty[Long]("webHeader.contactId").getOrElse(ConfigImpl.DEFAULT_WH_CONTACT_ID)

  override val orgBusinessUnit: String = getOptionalProperty[String]("webHeader.orgBusinessUnit").getOrElse(ConfigImpl.DEFAULT_WH_ORG_BUSINESS_UNIT)

  override val vssServiceTypeCode: String =
    getOptionalProperty[String]("webHeader.vssServiceTypeCode").getOrElse(ConfigImpl.DEFAULT_WH_VSS_SERVICE_TYPE_CODE)
  override val dmsServiceTypeCode: String =
    getOptionalProperty[String]("webHeader.dmsServiceTypeCode").getOrElse(ConfigImpl.DEFAULT_WH_DMS_SERVICE_TYPE_CODE)

  override val vrmRetentionEligibilityMicroServiceUrlBase: String =
    getOptionalProperty[String]("vrmRetentionEligibilityMicroServiceUrlBase").getOrElse(ConfigImpl.DEFAULT_URL_BASE)

  override val vrmRetentionRetainMicroServiceUrlBase: String =
    getOptionalProperty[String]("vrmRetentionRetainMicroServiceUrlBase").getOrElse(ConfigImpl.DEFAULT_URL_BASE)

  override val vehicleAndKeeperLookupMicroServiceBaseUrl: String =
    getOptionalProperty[String]("vehicleAndKeeperLookupMicroServiceUrlBase").getOrElse(ConfigImpl.DEFAULT_URL_BASE)

  override val vrmRetentionEligibilityMsRequestTimeout: Int =
    getOptionalProperty[Int]("vrmRetentionEligibility.requesttimeout").getOrElse(ConfigImpl.DEFAULT_REQUEST_TIMEOUT.seconds.toMillis.toInt)

  override val vrmRetentionRetainMsRequestTimeout: Int =
    getOptionalProperty[Int]("vrmRetentionRetain.requesttimeout").getOrElse(ConfigImpl.DEFAULT_REQUEST_TIMEOUT.seconds.toMillis.toInt)

  override val paymentSolveMicroServiceUrlBase: String =
    getOptionalProperty[String]("paymentSolveMicroServiceUrlBase").getOrElse(ConfigImpl.DEFAULT_URL_BASE)

  override val paymentSolveMsRequestTimeout: Int =
    getOptionalProperty[Int]("paymentSolve.ms.requesttimeout").getOrElse(ConfigImpl.DEFAULT_SOLVE_REQUEST_TIMEOUT.seconds.toMillis.toInt)

  override val vehicleAndKeeperLookupRequestTimeout: Int =
    getOptionalProperty[Int]("vehicleAndKeeperLookup.requesttimeout").getOrElse(ConfigImpl.DEFAULT_REQUEST_TIMEOUT.seconds.toMillis.toInt)

  override val isPrototypeBannerVisible: Boolean = getOptionalProperty[Boolean]("prototype.disclaimer").getOrElse(ConfigImpl.DEFAULT_PROTOTYPE_BANNER_ENABLED)

  override val googleAnalyticsTrackingId: Option[String] = getOptionalProperty[String]("googleAnalytics.id.retention")

  override val emailWhitelist: Option[List[String]] = getStringListProperty("email.whitelist")

  override val emailSenderAddress: String = getOptionalProperty[String]("email.senderAddress").getOrElse(ConfigImpl.DEFAULT_SENDER_EMAIL)

  override val cookieMaxAge = getOptionalProperty[Int]("application.cookieMaxAge").getOrElse(ConfigImpl.DEFAULT_COOKIE_MAX_AGE.minutes.toSeconds.toInt)

  override val storeBusinessDetailsMaxAge =
    getOptionalProperty[Int]("storeBusinessDetails.cookieMaxAge").getOrElse(ConfigImpl.DEFAULT_BUSINESS_DETAILS_MAX_AGE.days.toSeconds.toInt)

  override val auditMicroServiceUrlBase: String =
    getOptionalProperty[String]("auditMicroServiceUrlBase").getOrElse(ConfigImpl.DEFAULT_URL_BASE)

  override val auditMsRequestTimeout: Int =
    getOptionalProperty[Int]("audit.requesttimeout").getOrElse(ConfigImpl.DEFAULT_AUDIT_REQUEST_TIMEOUT.seconds.toMillis.toInt)

  // Email microservice
  override val emailServiceMicroServiceUrlBase: String =
    getOptionalProperty[String]("emailServiceMicroServiceUrlBase").getOrElse(ConfigImpl.DEFAULT_URL_BASE)
  override val emailServiceMsRequestTimeout: Int =
    getOptionalProperty[Int]("emailService.ms.requesttimeout").getOrElse(ConfigImpl.DEFAULT_REQUEST_TIMEOUT.seconds.toMillis.toInt)
  override val emailConfiguration: EmailConfiguration = EmailConfiguration(
    From(getProperty[String]("email.senderAddress"), ConfigImpl.EMAIL_FROM_NAME),
    From(getProperty[String]("email.feedbackAddress"), ConfigImpl.EMAILFEEDBACK_FROM_NAME),
    getStringListProperty("email.whitelist")
  )

  override val openingTimeMinOfDay: Int = getProperty[Int]("openingTimeMinOfDay")
  override val closingTimeMinOfDay: Int = getProperty[Int]("closingTimeMinOfDay")
  override val closingWarnPeriodMins: Int = getOptionalProperty[Int]("closingWarnPeriodMins").getOrElse(ConfigImpl.DEFAULT_CLOSING_WARN_PERIOD)
  override val closedDays: List[Int] = {
    println("closed days: " + getIntListProperty("closedDays").getOrElse(List()) )
    getIntListProperty("closedDays").getOrElse(List())
  }

  // TODO make property survey.url mandatory
  override val surveyUrl: Option[String] = getOptionalProperty[String]("survey.url")

  override val liveAgentEnvironmentId: Option[String] = {
    val liveAgentId: Option[String] = getOptionalProperty[String]("webchat.liveAgent.environmentId")
    liveAgentId.fold(Logger.info("Webchat functionality is not enabled"))
      {id => Logger.info("Webchat functionality is enabled")}
    liveAgentId
  }

  override val liveAgentButtonId: String = getProperty[String]("webchat.liveAgent.buttonId")
  override val liveAgentOrgId: String = getProperty[String]("webchat.liveAgent.orgId")
  override val liveAgentUrl: String = getProperty[String]("webchat.liveAgent.url")
  override val liveAgentjsUrl: String = getProperty[String]("webchat.liveAgent.jsUrl")

  override val failureCodeBlacklist: Option[List[String]] = getStringListProperty("webchat.failureCodes.blacklist")

}

object ConfigImpl {

  final val NotFound = "NOT FOUND"

  final val EMAIL_FROM_NAME = "DO-NOT-REPLY"
  final val EMAILFEEDBACK_FROM_NAME = "Feedback"

  //defaults
  final val DEFAULT_SENDER_EMAIL = ""
  final val DEFAULT_URL_BASE = ""
  final val DEFAULT_SECURECOOKIES = true
  final val DEFAULT_PROTOTYPE_BANNER_ENABLED = true
  final val DEFAULT_ENCRYPTEDCOOKIES = true

  final val DEFAULT_WH_APPLICATION_CODE = NotFound
  final val DEFAULT_WH_VSS_SERVICE_TYPE_CODE = NotFound
  final val DEFAULT_WH_DMS_SERVICE_TYPE_CODE = NotFound
  final val DEFAULT_WH_CHANNEL_CODE = NotFound
  final val DEFAULT_WH_CONTACT_ID = 0L
  final val DEFAULT_WH_ORG_BUSINESS_UNIT = NotFound

  final val DEFAULT_BUSINESS_DETAILS_MAX_AGE = 7 // days
  final val DEFAULT_CLOSING_WARN_PERIOD = 15 // minutes

  // timeouts
  final val DEFAULT_AUDIT_REQUEST_TIMEOUT = 30 //minutes
  final val DEFAULT_COOKIE_MAX_AGE = 30 // minutes
  final val DEFAULT_REQUEST_TIMEOUT = 30 // seconds
  final val DEFAULT_SOLVE_REQUEST_TIMEOUT = 5 // seconds
}

