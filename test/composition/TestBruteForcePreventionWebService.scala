package composition

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.{FORBIDDEN, OK}
import play.api.libs.ws.WSResponse
import services.fakes.BruteForcePreventionWebServiceConstants._
import services.fakes.VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberValid
import services.fakes._
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionWebService
import scala.concurrent.Future

class TestBruteForcePreventionWebService(permitted: Boolean = true) extends ScalaModule with MockitoSugar {

  def configure() = {
    val bruteForceStatus = if (permitted) OK else FORBIDDEN
    val bruteForcePreventionWebService = mock[BruteForcePreventionWebService]

    when(bruteForcePreventionWebService.callBruteForce(RegistrationNumberValid)).
      thenReturn(Future.successful(new FakeResponse(status = bruteForceStatus, fakeJson = responseFirstAttempt)))

    when(bruteForcePreventionWebService.callBruteForce(BruteForcePreventionWebServiceConstants.VrmAttempt2)).
      thenReturn(Future.successful(new FakeResponse(status = bruteForceStatus, fakeJson = responseSecondAttempt)))

    when(bruteForcePreventionWebService.callBruteForce(BruteForcePreventionWebServiceConstants.VrmLocked)).
      thenReturn(Future.successful(new FakeResponse(status = bruteForceStatus)))

    when(bruteForcePreventionWebService.callBruteForce(VrmThrows)).
      thenReturn(responseThrows)

    bind[BruteForcePreventionWebService].toInstance(bruteForcePreventionWebService)
  }

  private def responseThrows: Future[WSResponse] = Future.failed(new RuntimeException("This error is generated deliberately by a test"))
}
