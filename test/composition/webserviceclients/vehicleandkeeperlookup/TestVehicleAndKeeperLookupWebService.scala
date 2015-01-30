package composition.webserviceclients.vehicleandkeeperlookup

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.vehicleandkeeperlookup.TestVehicleAndKeeperLookupWebService.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.FakeResponse
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperDetailsRequest, VehicleAndKeeperDetailsResponse, VehicleAndKeeperLookupWebService}
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.vehicleAndKeeperDetailsResponseSuccess

import scala.concurrent.Future

final class TestVehicleAndKeeperLookupWebService(
                                                  statusAndResponse: (Int, Option[VehicleAndKeeperDetailsResponse]) = vehicleAndKeeperDetailsResponseSuccess
                                                  ) extends ScalaModule with MockitoSugar {

  val stub = {
    val webService: VehicleAndKeeperLookupWebService = mock[VehicleAndKeeperLookupWebService]
    when(webService.invoke(any[VehicleAndKeeperDetailsRequest], any[String])).thenReturn(Future.successful(createResponse(statusAndResponse)))
    webService
  }

  def configure() = bind[VehicleAndKeeperLookupWebService].toInstance(stub)
}

object TestVehicleAndKeeperLookupWebService {

  def createResponse(response: (Int, Option[VehicleAndKeeperDetailsResponse])) = {
    val (status, dto) = response
    val asJson = Json.toJson(dto)
    new FakeResponse(status = status, fakeJson = Some(asJson))
  }
}