package composition.webserviceclients.vehicleandkeeperlookup

import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup
import vehicleandkeeperlookup.VehicleAndKeeperLookupService
import vehicleandkeeperlookup.VehicleAndKeeperLookupServiceImpl

final class VehicleAndKeeperLookupServiceBinding extends ScalaModule {

  def configure() = bind[VehicleAndKeeperLookupService].to[VehicleAndKeeperLookupServiceImpl].asEagerSingleton()
}
