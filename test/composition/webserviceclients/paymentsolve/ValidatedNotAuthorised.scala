package composition.webserviceclients.paymentsolve

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.paymentsolve.TestPaymentSolveWebService.getResponseWithValidDefaults
import composition.webserviceclients.paymentsolve.TestPaymentSolveWebService.invalidStatus
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.OK
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.FakeResponse
import webserviceclients.paymentsolve._

import scala.concurrent.Future

final class ValidatedNotAuthorised extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveGetRequest], tracking = any[String])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = getResponseWithValidDefaults(status = invalidStatus))))
    webService
  }

  def configure() = bind[PaymentSolveWebService].toInstance(stub)
}
