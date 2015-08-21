package composition.webserviceclients.vehicleandkeeperlookup

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vehicleandkeeperlookup.TestVehicleAndKeeperLookupWebService.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.TrackingId
import common.webserviceclients.fakes.FakeResponse
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupRequest
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupResponse
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupWebService
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseSuccess

final class TestVehicleAndKeeperLookupWebService(statusAndResponse: (Int, Option[VehicleAndKeeperLookupResponse])
                                                  = vehicleAndKeeperDetailsResponseSuccess)
  extends ScalaModule with MockitoSugar {

  val stub = {
    val webService: VehicleAndKeeperLookupWebService = mock[VehicleAndKeeperLookupWebService]
    when(webService.invoke(any[VehicleAndKeeperLookupRequest], any[TrackingId]))
      .thenReturn(Future.successful(createResponse(statusAndResponse)))
    webService
  }

  def configure() = bind[VehicleAndKeeperLookupWebService].toInstance(stub)
}

object TestVehicleAndKeeperLookupWebService {

  def createResponse(response: (Int, Option[VehicleAndKeeperLookupResponse])) = {
    val (status, dto) = response
    val asJson = Json.toJson(dto)
    new FakeResponse(status = status, fakeJson = Some(asJson))
  }
}