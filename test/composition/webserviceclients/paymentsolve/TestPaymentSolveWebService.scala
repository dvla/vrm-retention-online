package composition.webserviceclients.paymentsolve

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.paymentsolve.TestPaymentSolveWebService.beginResponseWithValidDefaults
import composition.webserviceclients.paymentsolve.TestPaymentSolveWebService.cancelResponseWithValidDefaults
import composition.webserviceclients.paymentsolve.TestPaymentSolveWebService.getResponseWithValidDefaults
import composition.webserviceclients.paymentsolve.TestPaymentSolveWebService.updateResponseWithValidDefaults
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.OK
import play.api.libs.json.Json
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.FakeResponse
import webserviceclients.paymentsolve.{PaymentSolveBeginRequest, PaymentSolveBeginResponse}
import webserviceclients.paymentsolve.{PaymentSolveCancelRequest, PaymentSolveCancelResponse}
import webserviceclients.paymentsolve.{PaymentSolveGetRequest, PaymentSolveGetResponse}
import webserviceclients.paymentsolve.{PaymentSolveUpdateRequest, PaymentSolveUpdateResponse}
import webserviceclients.paymentsolve.PaymentSolveWebService

final class TestPaymentSolveWebService extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[PaymentSolveWebService]

    when(webService.invoke(request = any[PaymentSolveBeginRequest], tracking = any[TrackingId])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = beginResponseWithValidDefaults())))

    when(webService.invoke(request = any[PaymentSolveGetRequest], tracking = any[TrackingId])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = getResponseWithValidDefaults())))

    when(webService.invoke(request = any[PaymentSolveCancelRequest], tracking = any[TrackingId])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = cancelResponseWithValidDefaults())))

    when(webService.invoke(request = any[PaymentSolveUpdateRequest], tracking = any[TrackingId])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = updateResponseWithValidDefaults())))

    webService
  }

  def configure() = bind[PaymentSolveWebService].toInstance(stub)
}

object TestPaymentSolveWebService {

  val loadBalancerUrl = "http://somewhere-in-load-balancer-land:443"
  val beginWebPaymentUrl = "somewhere-in-payment-land"
  private[paymentsolve] val invalidStatus = "INVALID"
  private[paymentsolve] val invalidResponse = "INVALID"

  private[paymentsolve] def beginResponseWithValidDefaults(response: String = "validated",
                                                           status: String = "CARD_DETAILS") = {
    val paymentSolveBeginResponse = PaymentSolveBeginResponse(
      response = response,
      status = status,
      trxRef = Some("TODO"),
      redirectUrl = Some(beginWebPaymentUrl),
      isPrimaryUrl = true
    )
    val asJson = Json.toJson(paymentSolveBeginResponse)
    Some(asJson)
  }

  private[paymentsolve] def getResponseWithValidDefaults(response: String = "validated",
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

  private[paymentsolve] def cancelResponseWithValidDefaults(response: String = "validated",
                                                            status: String = "AUTHORISED") = {
    val paymentSolveCancelResponse = PaymentSolveCancelResponse(
      response = response,
      status = status
    )
    val asJson = Json.toJson(paymentSolveCancelResponse)
    Some(asJson)
  }

  private[paymentsolve] def updateResponseWithValidDefaults(response: String = "validated",
                                                            status: String = "CARD_DETAILS") = {
    val update = PaymentSolveUpdateResponse(
      response = response,
      status = status
    )
    val asJson = Json.toJson(update)
    Some(asJson)
  }
}