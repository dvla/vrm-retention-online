package webserviceclients.paymentsolve

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, postRequestedFor, urlEqualTo}
import composition.{TestConfig2, WithApplication}
import helpers.{UnitSpec, WireMockFixture}
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders

final class PaymentSolveWebServiceImplSpec extends UnitSpec with WireMockFixture {

  "invoke Begin" should {

    "send the serialised json request" in new WithApplication {
      val resultFuture = lookupService.invoke(request, trackingId)
      whenReady(resultFuture, timeout) { result =>
        wireMock.verifyThat(1, postRequestedFor(
          urlEqualTo(s"/payment/solve/beginWebPayment")
        ).withHeader(HttpHeaders.TrackingId, equalTo(trackingId)).
          withRequestBody(equalTo(Json.toJson(request).toString())))
      }
    }
  }

  private def lookupService = new PaymentSolveWebServiceImpl(
    config2 = new TestConfig2(paymentSolveMicroServiceUrlBase = s"http://localhost:$wireMockPort").build
  )

  private val trackingId = "track-id-test"

  private def request = PaymentSolveBeginRequest(
    transactionId = "transaction id",
    transNo = "trans no",
    vrm = "reg number",
    purchaseAmount = 9999,
    paymentCallback = "callback url"
  )

  private implicit val paymentSolveBeginFormat = Json.format[PaymentSolveBeginRequest]
}