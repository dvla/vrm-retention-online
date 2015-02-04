package composition.webserviceclients.bruteforceprevention

import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.{BruteForcePreventionService, BruteForcePreventionServiceImpl}

final class BruteForcePreventionServiceBinding extends ScalaModule {

  def configure() = bind[BruteForcePreventionService].to[BruteForcePreventionServiceImpl].asEagerSingleton()
}