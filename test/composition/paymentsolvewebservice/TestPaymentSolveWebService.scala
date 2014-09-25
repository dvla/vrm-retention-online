package composition.paymentsolvewebservice

import com.tzavellas.sse.guice.ScalaModule
import composition.paymentsolvewebservice.TestPaymentSolveWebService.responseWithValidDefaults
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import services.fakes.FakeResponse
import webserviceclients.paymentsolve.{PaymentSolveBeginRequest, PaymentSolveBeginResponse, PaymentSolveWebService}
import scala.concurrent.Future

class ValidatedCardDetails extends ScalaModule with MockitoSugar {

  def configure() = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveBeginRequest], tracking = any[String])).
      thenAnswer(
        new Answer[Future[WSResponse]] {
          override def answer(invocation: InvocationOnMock) = Future.successful {
            new FakeResponse(status = OK, fakeJson = responseWithValidDefaults())
          }
        }
      )
    bind[PaymentSolveWebService].toInstance(webService)
  }
}

class NotValidatedCardDetails extends ScalaModule with MockitoSugar {

  def configure() = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveBeginRequest], tracking = any[String])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = responseWithValidDefaults(response = "INVALID")))) // TODO replace with a realistic return value, though this will do for now.
    bind[PaymentSolveWebService].toInstance(webService)
  }
}

class ValidatedNotCardDetails extends ScalaModule with MockitoSugar {

  def configure() = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveBeginRequest], tracking = any[String])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = responseWithValidDefaults(status = "INVALID"))))
    bind[PaymentSolveWebService].toInstance(webService)
  }
}

class PaymentCallFails extends ScalaModule with MockitoSugar {

  def configure() = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveBeginRequest], tracking = any[String])).
      thenReturn(Future.failed(new RuntimeException("This error is generated deliberately by a stub for PaymentSolveWebService")))
    bind[PaymentSolveWebService].toInstance(webService)
  }
}

object TestPaymentSolveWebService {

  val beginWebPaymentUrl = "somewhere-in-payment-land"

  private[paymentsolvewebservice] def responseWithValidDefaults(response: String = "validated",
                                                                status: String = "CARD_DETAILS") = {
    val paymentSolveBeginResponse = PaymentSolveBeginResponse(
      response = response,
      status = status,
      trxRef = Some("TODO"),
      redirectUrl = Some(beginWebPaymentUrl)
    )
    val asJson = Json.toJson(paymentSolveBeginResponse)
    Some(asJson)
  }
}