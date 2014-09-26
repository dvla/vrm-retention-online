package composition.paymentsolvewebservice

import com.tzavellas.sse.guice.ScalaModule
import composition.paymentsolvewebservice.TestPaymentSolveWebService.beginResponseWithValidDefaults
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.OK
import services.fakes.FakeResponse
import webserviceclients.paymentsolve._
import scala.concurrent.Future

class ValidatedCardDetails extends ScalaModule with MockitoSugar {

  def configure() = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveBeginRequest], tracking = any[String])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = beginResponseWithValidDefaults())))
    bind[PaymentSolveWebService].toInstance(webService)
  }
}
