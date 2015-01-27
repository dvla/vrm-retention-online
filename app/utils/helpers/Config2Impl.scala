package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._

class Config2Impl extends Config2 {

  // Payment Service
  override def purchaseAmount: String = getOptionalProperty[String]("retention.purchaseAmountInPence").getOrElse("NOT FOUND")

  override def secureCookies = getOptionalProperty[Boolean]("secureCookies").getOrElse(true)

  override def ordnanceSurveyUseUprn: Boolean = getOptionalProperty[Boolean]("ordnancesurvey.useUprn").getOrElse(false)

  override def applicationCode: String = getOptionalProperty[String]("webHeader.applicationCode").getOrElse("NOT FOUND")
}
