package composition.paymentsolvewebservice

import com.tzavellas.sse.guice.ScalaModule
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
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import composition.paymentsolvewebservice.TestPaymentSolveWebService.beginWebPaymentUrl

class ValidatedCardDetails extends ScalaModule with MockitoSugar {

  def configure() = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveBeginRequest], tracking = any[String])).
      thenAnswer(
        new Answer[Future[WSResponse]] {
          override def answer(invocation: InvocationOnMock) = Future.successful {
            val args: Array[AnyRef] = invocation.getArguments
            val request = args(0).asInstanceOf[PaymentSolveBeginRequest] // Cast first argument.
            val response = PaymentSolveBeginResponse(
                response = "validated",
                status = "CARD_DETAILS",
                trxRef = Some("TODO"),
                redirectUrl = Some(beginWebPaymentUrl)
              )
            val asJson = Json.toJson(response)
            new FakeResponse(status = OK, fakeJson = Some(asJson))
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
      thenAnswer(
        new Answer[Future[WSResponse]] {
          override def answer(invocation: InvocationOnMock) = Future.successful {
            val args: Array[AnyRef] = invocation.getArguments
            val request = args(0).asInstanceOf[PaymentSolveBeginRequest] // Cast first argument.
            val response = PaymentSolveBeginResponse(
                response = "INVALID", // TODO replace with a realistic return value, though this will do for now.
                status = "CARD_DETAILS",
                trxRef = Some("TODO"),
                redirectUrl = Some(beginWebPaymentUrl)
              )
            val asJson = Json.toJson(response)
            new FakeResponse(status = OK, fakeJson = Some(asJson))
          }
        }
      )
    bind[PaymentSolveWebService].toInstance(webService)
  }
}

class ValidatedNotCardDetails extends ScalaModule with MockitoSugar {

  def configure() = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveBeginRequest], tracking = any[String])).
      thenAnswer(
        new Answer[Future[WSResponse]] {
          override def answer(invocation: InvocationOnMock) = Future.successful {
            val args: Array[AnyRef] = invocation.getArguments
            val request = args(0).asInstanceOf[PaymentSolveBeginRequest] // Cast first argument.
            val response = PaymentSolveBeginResponse(
                response = "validated",
                status = "INVALID",
                trxRef = Some("TODO"),
                redirectUrl = Some(beginWebPaymentUrl)
              )
            val asJson = Json.toJson(response)
            new FakeResponse(status = OK, fakeJson = Some(asJson))
          }
        }
      )
    bind[PaymentSolveWebService].toInstance(webService)
  }
}

class PaymentCallFails extends ScalaModule with MockitoSugar {

  def configure() = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveBeginRequest], tracking = any[String])).
      thenAnswer(
        new Answer[Future[WSResponse]] {
          override def answer(invocation: InvocationOnMock) = Future.failed(new RuntimeException("This error is generated deliberately by a stub for PaymentSolveWebService"))
        }
      )
    bind[PaymentSolveWebService].toInstance(webService)
  }
}

object TestPaymentSolveWebService {

  val beginWebPaymentUrl = "somewhere-in-payment-land"
}