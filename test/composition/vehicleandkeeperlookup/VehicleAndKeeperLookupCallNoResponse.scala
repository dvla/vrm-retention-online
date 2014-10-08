package composition.vehicleandkeeperlookup

import com.tzavellas.sse.guice.ScalaModule
import composition.vehicleandkeeperlookup.TestVehicleAndKeeperLookupWebService.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsNoResponse
import webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperDetailsRequest, VehicleAndKeeperLookupWebService}
import scala.concurrent.Future

class VehicleAndKeeperLookupCallNoResponse extends ScalaModule with MockitoSugar {

  def configure() = {
    val vehicleAndKeeperLookupWebService = mock[VehicleAndKeeperLookupWebService]
    when(vehicleAndKeeperLookupWebService.invoke(any[VehicleAndKeeperDetailsRequest], any[String])).
      thenReturn(Future.successful(createResponse(vehicleAndKeeperDetailsNoResponse)))
    bind[VehicleAndKeeperLookupWebService].toInstance(vehicleAndKeeperLookupWebService)
  }
}
