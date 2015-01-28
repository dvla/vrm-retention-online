package composition.paymentsolvewebservice

import com.tzavellas.sse.guice.ScalaModule
import composition.paymentsolvewebservice.TestPaymentSolveWebService.getResponseWithValidDefaults
import composition.paymentsolvewebservice.TestPaymentSolveWebService.updateResponseWithValidDefaults
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.OK
import webserviceclients.fakes.FakeResponse
import webserviceclients.paymentsolve.{PaymentSolveGetRequest, PaymentSolveUpdateRequest, PaymentSolveWebService}

import scala.concurrent.Future

final class ValidatedAuthorised extends ScalaModule with MockitoSugar {

  def configure() = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveGetRequest], tracking = any[String])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = getResponseWithValidDefaults())))
    when(webService.invoke(request = any[PaymentSolveUpdateRequest], tracking = any[String])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = updateResponseWithValidDefaults())))
    bind[PaymentSolveWebService].toInstance(webService)
  }
}
