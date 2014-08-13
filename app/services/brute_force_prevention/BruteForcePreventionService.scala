package services.brute_force_prevention

import models.domain.common.BruteForcePreventionViewModel
import scala.concurrent.Future

trait BruteForcePreventionService {
  def isVrmLookupPermitted(vrm: String): Future[BruteForcePreventionViewModel]
}