package composition.webserviceclients.vehicleandkeeperlookup

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vehicleandkeeperlookup.TestVehicleAndKeeperLookupWebService.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperDetailsRequest, VehicleAndKeeperLookupWebService}
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseDocRefNumberNotLatest

import scala.concurrent.Future

final class VehicleAndKeeperDetailsCallDocRefNumberNotLatest extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[VehicleAndKeeperLookupWebService]
    when(webService.invoke(any[VehicleAndKeeperDetailsRequest], any[String])).
      thenReturn(Future.successful(createResponse(vehicleAndKeeperDetailsResponseDocRefNumberNotLatest)))
    webService
  }

  def configure() = bind[VehicleAndKeeperLookupWebService].toInstance(stub)
}
