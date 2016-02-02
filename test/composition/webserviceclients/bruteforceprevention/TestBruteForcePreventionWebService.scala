package composition.webserviceclients.bruteforceprevention

import _root_.webserviceclients.fakes.BruteForcePreventionWebServiceConstants
import _root_.webserviceclients.fakes.BruteForcePreventionWebServiceConstants.VrmThrows
import _root_.webserviceclients.fakes.BruteForcePreventionWebServiceConstants.responseFirstAttempt
import _root_.webserviceclients.fakes.BruteForcePreventionWebServiceConstants.responseSecondAttempt
import _root_.webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberValid
import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.FORBIDDEN
import play.api.http.Status.OK
import play.api.libs.ws.WSResponse
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionWebService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.FakeResponse

final class TestBruteForcePreventionWebService(permitted: Boolean = true) extends ScalaModule with MockitoSugar {

  val stub = {
    val bruteForceStatus = if (permitted) OK else FORBIDDEN
    val bruteForcePreventionWebService = mock[BruteForcePreventionWebService]

    when(
      bruteForcePreventionWebService.callBruteForce(RegistrationNumberValid, TrackingId("default_test_tracking_id"))
    ).thenReturn(Future.successful(new FakeResponse(status = bruteForceStatus, fakeJson = responseFirstAttempt)))

    when(
      bruteForcePreventionWebService.callBruteForce(
        BruteForcePreventionWebServiceConstants.VrmAttempt2,
        TrackingId("default_test_tracking_id")
      )
    ).thenReturn(Future.successful(new FakeResponse(status = bruteForceStatus, fakeJson = responseSecondAttempt)))

    when(
      bruteForcePreventionWebService.callBruteForce(
        BruteForcePreventionWebServiceConstants.VrmLocked,
        TrackingId("default_test_tracking_id")
      )
    ).thenReturn(Future.successful(new FakeResponse(status = FORBIDDEN)))

    when(bruteForcePreventionWebService.callBruteForce(VrmThrows, TrackingId("default_test_tracking_id")))
      .thenReturn(responseThrows)

    when(bruteForcePreventionWebService.reset(any[String], any[TrackingId]))
      .thenReturn(Future.successful(new FakeResponse(status = play.api.http.Status.OK)))
    bruteForcePreventionWebService
  }

  def configure() = bind[BruteForcePreventionWebService].toInstance(stub)

  private def responseThrows: Future[WSResponse] = Future.failed(
    new RuntimeException("This error is generated deliberately by a stub for BruteForcePreventionWebService")
  )
}