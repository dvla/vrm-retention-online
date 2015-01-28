package composition.webserviceclients.vehicleandkeeperlookup

import _root_.webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperLookupService, VehicleAndKeeperLookupServiceImpl, VehicleAndKeeperLookupWebService, VehicleAndKeeperLookupWebServiceImpl}
import com.tzavellas.sse.guice.ScalaModule

final class VehicleAndKeeperLookupBinding extends ScalaModule {

  def configure() = {
    bind[VehicleAndKeeperLookupWebService].to[VehicleAndKeeperLookupWebServiceImpl].asEagerSingleton()
    bind[VehicleAndKeeperLookupService].to[VehicleAndKeeperLookupServiceImpl].asEagerSingleton()
  }
}