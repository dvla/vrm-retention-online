package utils.helpers

import play.api.Logger
import scala.concurrent.duration.DurationInt
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

final class ConfigImpl extends Config {

  val assetsUrl: Option[String] = getOptionalProperty[String]("assets.url")

  // Payment Service
  override val purchaseAmountInPence: String = getProperty[String]("retention.purchaseAmountInPence")

  override val secureCookies = getOptionalProperty[Boolean]("secureCookies").getOrElse(ConfigImpl.DefaultSecureCookies)

  override val encryptCookies = getOptionalProperty[Boolean]("encryptCookies").getOrElse(ConfigImpl.DefaultEncryptCookies)

  override val applicationCode: String = getProperty[String]("webHeader.applicationCode")
  override val channelCode: String = getProperty[String]("webHeader.channelCode")
  override val contactId: Long = getProperty[Long]("webHeader.contactId")
  override val orgBusinessUnit: String = getProperty[String]("webHeader.orgBusinessUnit")
  override val vssServiceTypeCode: String = getProperty[String]("webHeader.vssServiceTypeCode")
  override val dmsServiceTypeCode: String = getProperty[String]("webHeader.dmsServiceTypeCode")

  override val vrmRetentionEligibilityMicroServiceUrlBase: String = getProperty[String]("vrmRetentionEligibilityMicroServiceUrlBase")

  override val vrmRetentionRetainMicroServiceUrlBase: String = getProperty[String]("vrmRetentionRetainMicroServiceUrlBase")

  override val vehicleAndKeeperLookupMicroServiceBaseUrl: String = getProperty[String]("vehicleAndKeeperLookupMicroServiceUrlBase")

  override val vrmRetentionEligibilityMsRequestTimeout: Int =
    getOptionalProperty[Int]("vrmRetentionEligibility.requesttimeout")
      .getOrElse(ConfigImpl.DefaultRequestTimeoutSecs.seconds.toMillis.toInt)

  override val vrmRetentionRetainMsRequestTimeout: Int =
    getOptionalProperty[Int]("vrmRetentionRetain.requesttimeout")
      .getOrElse(ConfigImpl.DefaultRequestTimeoutSecs.seconds.toMillis.toInt)

  override val paymentSolveMicroServiceUrlBase: String = getProperty[String]("paymentSolveMicroServiceUrlBase")

  override val paymentSolveMsRequestTimeout: Int =
    getOptionalProperty[Int]("paymentSolve.ms.requesttimeout")
      .getOrElse(ConfigImpl.DefaultSolveRequestTimeoutSecs.seconds.toMillis.toInt)

  override val vehicleAndKeeperLookupRequestTimeout: Int =
    getOptionalProperty[Int]("vehicleAndKeeperLookup.requesttimeout")
      .getOrElse(ConfigImpl.DefaultRequestTimeoutSecs.seconds.toMillis.toInt)

  override val isPrototypeBannerVisible: Boolean = getOptionalProperty[Boolean]("prototype.disclaimer")
    .getOrElse(ConfigImpl.DefaultPrototypeBannerEnabled)

  override val googleAnalyticsTrackingId: Option[String] = getOptionalProperty[String]("googleAnalytics.id.retention")

  override val emailWhitelist: Option[List[String]] = getStringListProperty("email.whitelist")

  override val emailSenderAddress: String = getOptionalProperty[String]("email.senderAddress")
    .getOrElse(ConfigImpl.DefaultSenderEmail)

  override val cookieMaxAge = getOptionalProperty[Int]("application.cookieMaxAge")
    .getOrElse(ConfigImpl.DefaultCookieMaxAgeMins.minutes.toSeconds.toInt)

  override val storeBusinessDetailsMaxAge =
    getOptionalProperty[Int]("storeBusinessDetails.cookieMaxAge")
      .getOrElse(ConfigImpl.DefaultBusinessDetailsCookieMaxAgeDays.days.toSeconds.toInt)

  override val auditMicroServiceUrlBase: String = getProperty[String]("auditMicroServiceUrlBase")

  override val auditMsRequestTimeout: Int =
    getOptionalProperty[Int]("audit.requesttimeout")
      .getOrElse(ConfigImpl.DefaultAuditRequestTimeoutSecs.seconds.toMillis.toInt)

  // Email microservice
  override val emailServiceMicroServiceUrlBase: String = getProperty[String]("emailServiceMicroServiceUrlBase")
  override val emailServiceMsRequestTimeout: Int =
    getOptionalProperty[Int]("emailService.ms.requesttimeout")
      .getOrElse(ConfigImpl.DefaultRequestTimeoutSecs.seconds.toMillis.toInt)
  override val emailConfiguration: EmailConfiguration = EmailConfiguration(
    From(getProperty[String]("email.senderAddress"), ConfigImpl.EmailFromName),
    From(getProperty[String]("email.feedbackAddress"), ConfigImpl.EmailFeedbackFromName),
    getStringListProperty("email.whitelist")
  )

  override val openingTimeMinOfDay: Int = getProperty[Int]("openingTimeMinOfDay")
  override val closingTimeMinOfDay: Int = getProperty[Int]("closingTimeMinOfDay")
  override val closingWarnPeriodMins: Int = getOptionalProperty[Int]("closingWarnPeriodMins")
    .getOrElse(ConfigImpl.DefaultClosingWarnPeriodMins)
  override val closedDays: List[Int] = {
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

  final val EmailFromName = "DO-NOT-REPLY"
  final val EmailFeedbackFromName = "Feedback"

  //defaults
  final val DefaultSenderEmail = ""
  final val DefaultSecureCookies = true
  final val DefaultPrototypeBannerEnabled = true
  final val DefaultEncryptCookies = true
  final val DefaultBusinessDetailsCookieMaxAgeDays = 7
  final val DefaultClosingWarnPeriodMins = 15
  final val DefaultCookieMaxAgeMins = 30
  final val DefaultAuditRequestTimeoutSecs = 30
  final val DefaultRequestTimeoutSecs = 30
  final val DefaultSolveRequestTimeoutSecs = 5
}