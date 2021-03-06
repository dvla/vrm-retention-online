package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupConfig
import uk.gov.dvla.vehicles.presentation.common.services.SEND.EmailConfiguration
import uk.gov.dvla.vehicles.presentation.common.utils.helpers.CommonConfig

trait Config extends VehicleLookupConfig with CommonConfig {

  val assetsUrl: Option[String]

  // Payment Service
  val purchaseAmountInPence: String

  //  // Micro-service config
  val vehicleAndKeeperLookupMicroServiceBaseUrl: String

  val vrmRetentionEligibilityMicroServiceUrlBase: String

  val vrmRetentionEligibilityMsRequestTimeout: Int

  val vrmRetentionRetainMicroServiceUrlBase: String

  val vrmRetentionRetainMsRequestTimeout: Int

  val paymentSolveMicroServiceUrlBase: String

  val paymentSolveMsRequestTimeout: Int

  val emailServiceMicroServiceUrlBase: String
  val emailServiceMsRequestTimeout: Int
  val emailConfiguration: EmailConfiguration

  val vehicleAndKeeperLookupRequestTimeout: Int

  // Google analytics
  val googleAnalyticsTrackingId: Option[String]

   // Email Service
  val emailWhitelist: Option[List[String]]
  val emailSenderAddress: String

  // Cookie flags
  val secureCookies: Boolean

  val encryptCookies: Boolean

  val cookieMaxAge: Int

  val storeBusinessDetailsMaxAge: Int

  // Audit microservice
  val auditMicroServiceUrlBase: String

  val auditMsRequestTimeout: Int

  val openingTimeMinOfDay: Int
  val closingTimeMinOfDay: Int

  val closingWarnPeriodMins: Int

  val closedDays: List[Int]

  // Survey
  val surveyUrl: Option[String]

  // Web chat Salesforce live agent
  val liveAgentEnvironmentId: Option[String]
  val liveAgentButtonId: String
  val liveAgentOrgId: String
  val liveAgentUrl: String
  val liveAgentjsUrl: String

  val failureCodeBlacklist: Option[List[String]]
}
