package services.brute_force_prevention

import scala.concurrent.Future
import models.domain.common.BruteForcePreventionViewModel

trait BruteForcePreventionService {
  def isVrmLookupPermitted(vrm: String): Future[BruteForcePreventionViewModel]
}