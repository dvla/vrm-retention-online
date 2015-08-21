package composition.webserviceclients.vehicleandkeeperlookup

import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup
import vehicleandkeeperlookup.VehicleAndKeeperLookupWebService
import vehicleandkeeperlookup.VehicleAndKeeperLookupWebServiceImpl

final class VehicleAndKeeperLookupWebServiceBinding extends ScalaModule {

  def configure() = bind[VehicleAndKeeperLookupWebService].to[VehicleAndKeeperLookupWebServiceImpl].asEagerSingleton()
}