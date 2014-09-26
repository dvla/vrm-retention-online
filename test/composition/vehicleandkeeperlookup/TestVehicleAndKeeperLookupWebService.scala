package composition.vehicleandkeeperlookup

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants._
import webserviceclients.fakes._
import webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperDetailsRequest, VehicleAndKeeperDetailsResponse, VehicleAndKeeperLookupWebService}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TestVehicleAndKeeperLookupWebService extends ScalaModule with MockitoSugar {

  def configure() = {
    val vehicleAndKeeperLookupWebService = mock[VehicleAndKeeperLookupWebService]
    when(vehicleAndKeeperLookupWebService.invoke(any[VehicleAndKeeperDetailsRequest], any[String])).
      thenAnswer(
        new Answer[Future[WSResponse]] {
          override def answer(invocation: InvocationOnMock) = Future {
            val args: Array[AnyRef] = invocation.getArguments
            val request = args(0).asInstanceOf[VehicleAndKeeperDetailsRequest] // Cast first argument.
            val (responseStatus, response) = {
              request.referenceNumber match {
                case "99999999991" => vehicleAndKeeperDetailsResponseVRMNotFound
                case "99999999992" => vehicleAndKeeperDetailsResponseDocRefNumberNotLatest
                case "99999999999" => vehicleAndKeeperDetailsResponseNotFoundResponseCode
                case _ => vehicleAndKeeperDetailsResponseSuccess
              }
            }
            val responseAsJson = Json.toJson(response)
            new FakeResponse(status = responseStatus, fakeJson = Some(responseAsJson)) // Any call to a webservice will always return this successful response.
          }
        }
      )
    bind[VehicleAndKeeperLookupWebService].toInstance(vehicleAndKeeperLookupWebService)
  }
}

object TestVehicleAndKeeperLookupWebService {

  def createResponse(response: (Int, Option[VehicleAndKeeperDetailsResponse])) = {
    val (status, dto) = response
    val asJson = Json.toJson(dto)
    new FakeResponse(status = status, fakeJson = Some(asJson))
  }
}