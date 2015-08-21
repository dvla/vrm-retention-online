package composition.webserviceclients.paymentsolve

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.paymentsolve.TestPaymentSolveWebService.cancelResponseWithValidDefaults
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.OK
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.FakeResponse
import webserviceclients.paymentsolve.{PaymentSolveCancelRequest, PaymentSolveWebService}

final class CancelValidated extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveCancelRequest], tracking = any[TrackingId])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = cancelResponseWithValidDefaults())))
    webService
  }

  def configure() = bind[PaymentSolveWebService].toInstance(stub)
}
