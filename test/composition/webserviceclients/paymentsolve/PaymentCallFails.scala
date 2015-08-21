package composition.webserviceclients.paymentsolve

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import webserviceclients.paymentsolve.PaymentSolveBeginRequest
import webserviceclients.paymentsolve.PaymentSolveCancelRequest
import webserviceclients.paymentsolve.PaymentSolveGetRequest
import webserviceclients.paymentsolve.PaymentSolveUpdateRequest
import webserviceclients.paymentsolve.PaymentSolveWebService
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

final class PaymentCallFails extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveBeginRequest], tracking = any[TrackingId])).
      thenReturn(
        Future.failed(new RuntimeException("This error is generated deliberately by a stub for PaymentSolveWebService"))
      )
    when(webService.invoke(request = any[PaymentSolveGetRequest], tracking = any[TrackingId])).
      thenReturn(
        Future.failed(new RuntimeException("This error is generated deliberately by a stub for PaymentSolveWebService"))
      )
    when(webService.invoke(request = any[PaymentSolveCancelRequest], tracking = any[TrackingId])).
      thenReturn(
        Future.failed(new RuntimeException("This error is generated deliberately by a stub for PaymentSolveWebService"))
      )
    when(webService.invoke(request = any[PaymentSolveUpdateRequest], tracking = any[TrackingId])).
      thenReturn(
        Future.failed(new RuntimeException("This error is generated deliberately by a stub for PaymentSolveWebService"))
      )
    webService
  }

  def configure() = bind[PaymentSolveWebService].toInstance(stub)
}