package composition

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Mockito.when
import org.mockito.Matchers.any
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.{FORBIDDEN, OK}
import play.api.libs.ws.WSResponse
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionWebService
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberValid
import webserviceclients.fakes.FakeResponse
import webserviceclients.fakes.BruteForcePreventionWebServiceConstants
import webserviceclients.fakes.BruteForcePreventionWebServiceConstants.responseFirstAttempt
import webserviceclients.fakes.BruteForcePreventionWebServiceConstants.responseSecondAttempt
import webserviceclients.fakes.BruteForcePreventionWebServiceConstants.VrmThrows
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

    when(bruteForcePreventionWebService.reset(any[String])).
      thenReturn(Future.successful(new FakeResponse(status = play.api.http.Status.OK)))

    bind[BruteForcePreventionWebService].toInstance(bruteForcePreventionWebService)
  }

  private def responseThrows: Future[WSResponse] = Future.failed(new RuntimeException("This error is generated deliberately by a stub for BruteForcePreventionWebService"))
}
