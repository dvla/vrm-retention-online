package models.domain.common

import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey

final case class BruteForcePreventionViewModel(permitted: Boolean,
                                               attempts: Int,
                                               maxAttempts: Int,
                                               dateTimeISOChronology: String)

object BruteForcePreventionViewModel {

  implicit final val JsonFormat = Json.format[BruteForcePreventionViewModel]
  final val BruteForcePreventionViewModelCacheKey = "bruteForcePreventionViewModel"
  implicit final val Key = CacheKey[BruteForcePreventionViewModel](BruteForcePreventionViewModelCacheKey)

  def from(permitted: Boolean,
           response: BruteForcePreventionResponse,
           dateService: DateService,
           maxAttempts: Int): BruteForcePreventionViewModel = {
    BruteForcePreventionViewModel(permitted,
      attempts = response.attempts + 1,
      maxAttempts = maxAttempts,
      dateTimeISOChronology = dateService.dateTimeISOChronology // Save the time we locked in case we need to display it on a page e.g. vrm-locked page.
    )
  }
}