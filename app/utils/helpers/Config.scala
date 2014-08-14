package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.{getDurationProperty, getProperty}
import scala.concurrent.duration.DurationInt

class Config {

  val isCsrfPreventionEnabled = getProperty("csrf.prevention", default = true)

  // Micro-service config
  val vehicleAndKeeperLookupMicroServiceBaseUrl: String = getProperty("vehicleAndKeeperLookupMicroServiceUrlBase", "NOT FOUND")
  val vrmRetentionEligibilityMicroServiceUrlBase: String = getProperty("vrmRetentionEligibilityMicroServiceUrlBase", "NOT FOUND")
  val vrmRetentionEligibilityMsRequestTimeout: Int = getProperty("vrmRetentionEligibility.ms.requesttimeout", 5.seconds.toMillis.toInt)
  val vrmRetentionRetainMicroServiceUrlBase: String = getProperty("vrmRetentionRetainMicroServiceUrlBase", "NOT FOUND")
  val vrmRetentionRetainMsRequestTimeout: Int = getProperty("vrmRetentionRetain.ms.requesttimeout", 5.seconds.toMillis.toInt)

  // Ordnance survey config
  val ordnanceSurveyMicroServiceUrl: String = getProperty("ordnancesurvey.ms.url", "NOT FOUND")
  val ordnanceSurveyRequestTimeout: Int = getProperty("ordnancesurvey.requesttimeout", 5.seconds.toMillis.toInt)

  // Brute force prevention config
  val bruteForcePreventionMicroServiceBaseUrl: String = getProperty("bruteForcePreventionMicroServiceBase", "NOT FOUND")
  val bruteForcePreventionTimeout: Int = getProperty("bruteForcePrevention.requesttimeout", 5.seconds.toMillis.toInt)
  val isBruteForcePreventionEnabled: Boolean = getProperty("bruteForcePrevention.enabled", default = true)
  val bruteForcePreventionServiceNameHeader: String = getProperty("bruteForcePrevention.headers.serviceName", "")
  val bruteForcePreventionMaxAttemptsHeader: Int = getProperty("bruteForcePrevention.headers.maxAttempts", 3)
  val bruteForcePreventionExpiryHeader: String = getProperty("bruteForcePrevention.headers.expiry", "")

  // Prototype message in html
  val isPrototypeBannerVisible: Boolean = getProperty("prototype.disclaimer", default = true)

  // Prototype survey URL
  val prototypeSurveyUrl: String = getProperty("survey.url", "")
  val prototypeSurveyPrepositionInterval: Long = getDurationProperty("survey.interval", 7.days.toMillis)

  // Google analytics
  val isGoogleAnalyticsEnabled: Boolean = getProperty("googleAnalytics.enabled", default = true)

  // Progress step indicator
  val isProgressBarEnabled: Boolean = getProperty("progressBar.enabled", default = true)
}