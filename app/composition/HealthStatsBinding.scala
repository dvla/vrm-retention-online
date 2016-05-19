package composition

import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStats

final class HealthStatsBinding extends ScalaModule {

  def configure() = bind[HealthStats].asEagerSingleton()
}
