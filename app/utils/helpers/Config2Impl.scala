package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._

import scala.concurrent.duration.DurationInt

class Config2Impl extends Config2 {

  // Payment Service
  override def purchaseAmount: String = getOptionalProperty[String]("retention.purchaseAmountInPence").getOrElse("NOT FOUND")

  override def secureCookies = getOptionalProperty[Boolean]("secureCookies").getOrElse(true)

  override def ordnanceSurveyUseUprn: Boolean = getOptionalProperty[Boolean]("ordnancesurvey.useUprn").getOrElse(false)

  override def applicationCode: String = getOptionalProperty[String]("webHeader.applicationCode").getOrElse("NOT FOUND")

  override def channelCode: String = getOptionalProperty[String]("webHeader.channelCode").getOrElse("NOT FOUND")

  override def contactId: Long = getOptionalProperty[Long]("webHeader.contactId").getOrElse(0L)

  override def serviceTypeCode: String = getOptionalProperty[String]("webHeader.serviceTypeCode").getOrElse("NOT FOUND")

  override def vrmRetentionEligibilityMicroServiceUrlBase: String = getOptionalProperty[String]("vrmRetentionEligibilityMicroServiceUrlBase").getOrElse("NOT FOUND")

  override def vrmRetentionRetainMicroServiceUrlBase: String = getOptionalProperty[String]("vrmRetentionRetainMicroServiceUrlBase").getOrElse("NOT FOUND")

  override def vehicleAndKeeperLookupMicroServiceBaseUrl: String = getOptionalProperty[String]("vehicleAndKeeperLookupMicroServiceUrlBase").getOrElse("NOT FOUND")

  override def vrmRetentionEligibilityMsRequestTimeout: Int = getOptionalProperty[Int]("vrmRetentionEligibility.requesttimeout").getOrElse(30.seconds.toMillis.toInt)

  override def vrmRetentionRetainMsRequestTimeout: Int = getOptionalProperty[Int]("vrmRetentionRetain.requesttimeout").getOrElse(30.seconds.toMillis.toInt)

  override def paymentSolveMicroServiceUrlBase: String = getOptionalProperty[String]("paymentSolveMicroServiceUrlBase").getOrElse("NOT FOUND")

  override def paymentSolveMsRequestTimeout: Int = getOptionalProperty[Int]("paymentSolve.ms.requesttimeout").getOrElse(5.seconds.toMillis.toInt)

  override def vehicleAndKeeperLookupRequestTimeout: Int = getOptionalProperty[Int]("vehicleAndKeeperLookup.requesttimeout").getOrElse(30.seconds.toMillis.toInt)

  override def isPrototypeBannerVisible: Boolean = getOptionalProperty[Boolean]("prototype.disclaimer").getOrElse(true)
}
