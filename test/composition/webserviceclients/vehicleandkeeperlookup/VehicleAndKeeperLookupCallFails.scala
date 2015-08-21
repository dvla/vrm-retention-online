package composition.webserviceclients.vehicleandkeeperlookup

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.TrackingId
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupRequest
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupWebService

final class VehicleAndKeeperLookupCallFails extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[VehicleAndKeeperLookupWebService]
    when(webService.invoke(any[VehicleAndKeeperLookupRequest], any[TrackingId]))
      .thenReturn(
        Future.failed(
          new RuntimeException("This error is generated deliberately by a stub for VehicleAndKeeperLookupWebService")
        )
      )
    webService
  }

  def configure() = {
    bind[VehicleAndKeeperLookupWebService].toInstance(stub)
  }
}