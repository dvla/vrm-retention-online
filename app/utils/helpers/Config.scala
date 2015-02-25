package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupConfig

trait Config extends VehicleLookupConfig {

  // Payment Service
  def purchaseAmount: String

  //  // Micro-service config
  def vehicleAndKeeperLookupMicroServiceBaseUrl: String

  def vrmRetentionEligibilityMicroServiceUrlBase: String

  def vrmRetentionEligibilityMsRequestTimeout: Int

  def vrmRetentionRetainMicroServiceUrlBase: String

  def vrmRetentionRetainMsRequestTimeout: Int

  def paymentSolveMicroServiceUrlBase: String

  def paymentSolveMsRequestTimeout: Int

  def emailServiceMicroServiceUrlBase: String
  def emailServiceMsRequestTimeout: Int

  // Ordnance survey config
  def ordnanceSurveyUseUprn: Boolean

  def vehicleAndKeeperLookupRequestTimeout: Int

  // Prototype message in html
  def isPrototypeBannerVisible: Boolean

  // Google analytics
  def googleAnalyticsTrackingId: Option[String]

  // Progress step indicator
  def isProgressBarEnabled: Boolean

  // Rabbit-MQ
  def rabbitmqHost: String

  def rabbitmqPort: Int

  def rabbitmqQueue: String

  def rabbitmqUsername: String

  def rabbitmqPassword: String

  def rabbitmqVirtualHost: String

  // Email Service
  def emailWhitelist: Option[List[String]]
  def emailSenderAddress: String

  // Cookie flags
  def secureCookies: Boolean

  def encryptCookies: Boolean

  def cookieMaxAge: Int

  def storeBusinessDetailsMaxAge: Int

  // Audit microservice
  def auditMicroServiceUrlBase: String

  def auditMsRequestTimeout: Int

  def opening: Int

  def closing: Int
}
