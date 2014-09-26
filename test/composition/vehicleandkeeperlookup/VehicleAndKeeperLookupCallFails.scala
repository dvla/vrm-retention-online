package composition.vehicleandkeeperlookup

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperDetailsRequest, VehicleAndKeeperLookupWebService}
import scala.concurrent.Future

class VehicleAndKeeperLookupCallFails extends ScalaModule with MockitoSugar {

  def configure() = {
    val vehicleAndKeeperLookupWebService = mock[VehicleAndKeeperLookupWebService]
    when(vehicleAndKeeperLookupWebService.invoke(any[VehicleAndKeeperDetailsRequest], any[String])).
      thenReturn(Future.failed(new RuntimeException("This error is generated deliberately by a stub for VehicleAndKeeperLookupWebService")))
    bind[VehicleAndKeeperLookupWebService].toInstance(vehicleAndKeeperLookupWebService)
  }
}
