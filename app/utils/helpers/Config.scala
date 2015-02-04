package utils.helpers

trait Config {

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
  def emailSmtpHost: String

  def emailSmtpPort: Int

  def emailSmtpTls: Boolean

  def emailSmtpUser: String

  def emailSmtpPassword: String

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

  //  // Web headers
  def applicationCode: String

  def vssServiceTypeCode: String
  def dmsServiceTypeCode: String

  //  def orgBusinessUnit: String
  def channelCode: String

  def contactId: Long

  def orgBusinessUnit: String
}