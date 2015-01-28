package composition.webserviceclients.paymentsolve

import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import webserviceclients.paymentsolve._

import scala.concurrent.Future

final class PaymentCallFails extends ScalaModule with MockitoSugar {

  def configure() = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveBeginRequest], tracking = any[String])).
      thenReturn(Future.failed(new RuntimeException("This error is generated deliberately by a stub for PaymentSolveWebService")))
    when(webService.invoke(request = any[PaymentSolveGetRequest], tracking = any[String])).
      thenReturn(Future.failed(new RuntimeException("This error is generated deliberately by a stub for PaymentSolveWebService")))
    when(webService.invoke(request = any[PaymentSolveCancelRequest], tracking = any[String])).
      thenReturn(Future.failed(new RuntimeException("This error is generated deliberately by a stub for PaymentSolveWebService")))
    when(webService.invoke(request = any[PaymentSolveUpdateRequest], tracking = any[String])).
      thenReturn(Future.failed(new RuntimeException("This error is generated deliberately by a stub for PaymentSolveWebService")))
    bind[PaymentSolveWebService].toInstance(webService)
  }
}
