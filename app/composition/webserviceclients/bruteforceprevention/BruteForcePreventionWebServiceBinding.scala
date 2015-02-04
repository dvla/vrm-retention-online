package composition.webserviceclients.bruteforceprevention

import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionWebService

final class BruteForcePreventionWebServiceBinding extends ScalaModule {

  def configure() = bind[BruteForcePreventionWebService].to[bruteforceprevention.WebServiceImpl].asEagerSingleton()
}
