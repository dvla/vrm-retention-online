package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._

trait Config2 {

  // Payment Service
  def purchaseAmount: String

//  // Micro-service config
//  def vehicleAndKeeperLookupMicroServiceBaseUrl: String
//  def vrmAssignEligibilityMicroServiceUrlBase: String
//  def vrmAssignFulfilMicroServiceUrlBase: String
//  def paymentSolveMicroServiceUrlBase: String
//  def paymentSolveMsRequestTimeout: Int
//
//  // Ordnance survey config
//  def ordnanceSurveyMicroServiceUrl: String
//  def ordnanceSurveyRequestTimeout: Int
  def ordnanceSurveyUseUprn: Boolean
//
//  def vehicleAndKeeperLookupRequestTimeout: Int
//  def vrmAssignEligibilityRequestTimeout: Int
//  def vrmAssignFulfilRequestTimeout: Int
//
//  // Prototype message in html
//  def isPrototypeBannerVisible: Boolean
//
//  // Prototype survey URL
//  def prototypeSurveyUrl: String
//  def prototypeSurveyPrepositionInterval: Long
//
//  // Google analytics
//  def googleAnalyticsTrackingId: Option[String]
//
//  // Progress step indicator
//  def isProgressBarEnabled: Boolean
//
//  // Rabbit-MQ
//  def rabbitmqHost: String
//  def rabbitmqPort: Int
//  def rabbitmqQueue: String
//  def rabbitmqUsername:String
//  def rabbitmqPassword: String
//  def rabbitmqVirtualHost: String
//
//  // Payment Service
//  def renewalFee: String
//  def renewalFeeAbolitionDate: String
//
//  // Email Service
//  def emailSmtpHost: String
//  def emailSmtpPort: Int
//  def emailSmtpSsl: Boolean
//  def emailSmtpTls: Boolean
//  def emailSmtpUser: String
//  def emailSmtpPassword: String
//  def emailWhitelist: Option[List[String]]
//  def emailSenderAddress: String
//
//  // Cookie flags
  def secureCookies: Boolean
//  def cookieMaxAge: Int
//  def storeBusinessDetailsMaxAge:Int
//
//  // Audit microservice
//  def auditMicroServiceUrlBase: String
//  def auditMsRequestTimeout: Int
//
//  // Web headers
//  def applicationCode: String
//  def serviceTypeCode: String
//  def orgBusinessUnit: String
//  def channelCode: String
//  def contactId: Long
}