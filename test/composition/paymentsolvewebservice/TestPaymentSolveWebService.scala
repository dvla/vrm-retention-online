package composition.paymentsolvewebservice

import com.tzavellas.sse.guice.ScalaModule
import composition.paymentsolvewebservice.TestPaymentSolveWebService.{beginResponseWithValidDefaults, getResponseWithValidDefaults, invalidResponse, invalidStatus}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.OK
import play.api.libs.json.Json
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

class NotValidatedCardDetails extends ScalaModule with MockitoSugar {

  def configure() = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveBeginRequest], tracking = any[String])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = beginResponseWithValidDefaults(response = invalidResponse))))
    bind[PaymentSolveWebService].toInstance(webService)
  }
}

class ValidatedNotCardDetails extends ScalaModule with MockitoSugar {

  def configure() = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveBeginRequest], tracking = any[String])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = beginResponseWithValidDefaults(status = invalidStatus))))
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

class NotValidatedAuthorised extends ScalaModule with MockitoSugar {

  def configure() = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveGetRequest], tracking = any[String])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = getResponseWithValidDefaults(response = invalidResponse))))
    bind[PaymentSolveWebService].toInstance(webService)
  }
}

class ValidatedNotAuthorised extends ScalaModule with MockitoSugar {

  def configure() = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveGetRequest], tracking = any[String])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = getResponseWithValidDefaults(status = invalidStatus))))
    bind[PaymentSolveWebService].toInstance(webService)
  }
}

class ValidatedAuthorised extends ScalaModule with MockitoSugar {

  def configure() = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveGetRequest], tracking = any[String])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = getResponseWithValidDefaults())))
    bind[PaymentSolveWebService].toInstance(webService)
  }
}

object TestPaymentSolveWebService {

  val beginWebPaymentUrl = "somewhere-in-payment-land"
  // TODO replace with a realistic invalid response value.
  private[paymentsolvewebservice] val invalidStatus = "INVALID"
  // TODO replace with a realistic invalid status value.
  private[paymentsolvewebservice] val invalidResponse = "INVALID"

  private[paymentsolvewebservice] def beginResponseWithValidDefaults(response: String = "validated",
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

  private[paymentsolvewebservice] def getResponseWithValidDefaults(response: String = "validated",
                                                                   status: String = "AUTHORISED") = {
    val paymentSolveGetResponse = PaymentSolveGetResponse(
      response = response,
      status = status,
      authcode = Some("TODO"),
      maskedPAN = Some("TODO")
    )
    val asJson = Json.toJson(paymentSolveGetResponse)
    Some(asJson)
  }
}