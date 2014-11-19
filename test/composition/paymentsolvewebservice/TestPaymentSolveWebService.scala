package composition.paymentsolvewebservice

import com.tzavellas.sse.guice.ScalaModule
import composition.paymentsolvewebservice.TestPaymentSolveWebService.{beginResponseWithValidDefaults, cancelResponseWithValidDefaults, getResponseWithValidDefaults, updateResponseWithValidDefaults}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.OK
import play.api.libs.json.Json
import webserviceclients.fakes.FakeResponse
import webserviceclients.paymentsolve._

import scala.concurrent.Future

class TestPaymentSolveWebService extends ScalaModule with MockitoSugar {

  def configure() = {
    val webService = mock[PaymentSolveWebService]

    when(webService.invoke(request = any[PaymentSolveBeginRequest], tracking = any[String])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = beginResponseWithValidDefaults())))

    when(webService.invoke(request = any[PaymentSolveGetRequest], tracking = any[String])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = getResponseWithValidDefaults())))

    when(webService.invoke(request = any[PaymentSolveCancelRequest], tracking = any[String])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = cancelResponseWithValidDefaults())))

    when(webService.invoke(request = any[PaymentSolveUpdateRequest], tracking = any[String])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = updateResponseWithValidDefaults())))


    bind[PaymentSolveWebService].toInstance(webService)
  }
}

object TestPaymentSolveWebService {

  val loadBalancerUrl = "somewhere-in-load-balancer-land"
  val beginWebPaymentUrl = "somewhere-in-payment-land"
  private[paymentsolvewebservice] val invalidStatus = "INVALID"
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
      maskedPAN = Some("TODO"),
      merchantTransactionId = Some("TODO"),
      paymentType = Some("TODO"),
      cardType = Some("TODO"),
      purchaseAmount = Some(8000)
    )
    val asJson = Json.toJson(paymentSolveGetResponse)
    Some(asJson)
  }

  private[paymentsolvewebservice] def cancelResponseWithValidDefaults(response: String = "validated",
                                                                      status: String = "AUTHORISED") = {
    val paymentSolveCancelResponse = PaymentSolveCancelResponse(
      response = response,
      status = status
    )
    val asJson = Json.toJson(paymentSolveCancelResponse)
    Some(asJson)
  }

  private[paymentsolvewebservice] def updateResponseWithValidDefaults(response: String = "validated",
                                                                      status: String = "CARD_DETAILS") = {
    val update = PaymentSolveUpdateResponse(
      response = response,
      status = status
    )
    val asJson = Json.toJson(update)
    Some(asJson)
  }
}