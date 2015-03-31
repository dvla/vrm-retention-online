package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupConfig
import uk.gov.dvla.vehicles.presentation.common.services.SEND.EmailConfiguration

trait Config extends VehicleLookupConfig {

  val assetsUrl: Option[String]

  // Payment Service
  val purchaseAmount: String

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

  // Ordnance survey config
  val ordnanceSurveyUseUprn: Boolean

  val vehicleAndKeeperLookupRequestTimeout: Int

  // Prototype message in html
  val isPrototypeBannerVisible: Boolean

  // Google analytics
  val googleAnalyticsTrackingId: Option[String]

  // Progress step indicator
  val isProgressBarEnabled: Boolean

  // Rabbit-MQ
  val rabbitmqHost: String

  val rabbitmqPort: Int

  val rabbitmqQueue: String

  val rabbitmqUsername: String

  val rabbitmqPassword: String

  val rabbitmqVirtualHost: String

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

  val opening: Int

  val closing: Int

  val closingWarnPeriodMins: Int
}
