package composition

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.{any, _}
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.{FORBIDDEN, OK}
import services.fakes.BruteForcePreventionWebServiceConstants._
import services.fakes._
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionWebService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TestBruteForcePreventionWebService extends ScalaModule with MockitoSugar {

  def configure() = {
    val bruteForcePreventionWebService = mock[BruteForcePreventionWebService]
    when(bruteForcePreventionWebService.callBruteForce(any[String])).thenReturn(Future {
      new FakeResponse(status = OK, fakeJson = responseFirstAttempt)
    })
    when(bruteForcePreventionWebService.callBruteForce(matches(VrmLocked))).thenReturn(Future {
      new FakeResponse(status = FORBIDDEN)
    })
    bind[BruteForcePreventionWebService].toInstance(bruteForcePreventionWebService)
  }
}
