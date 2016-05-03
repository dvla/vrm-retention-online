package webserviceclients.paymentsolve

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import composition.TestConfig
import helpers.TestWithApplication
import helpers.UnitSpec
import helpers.WireMockFixture
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders

class PaymentSolveWebServiceImplSpec extends UnitSpec with WireMockFixture {

  "invoke Begin" should {
    "send the serialised json request" in new TestWithApplication {
      val resultFuture = lookupService.invoke(request, trackingId)
      whenReady(resultFuture, timeout) { result =>
        wireMock.verifyThat(1, postRequestedFor(
          urlEqualTo(s"/payment/solve/beginWebPayment")
        ).withHeader(HttpHeaders.TrackingId, equalTo(trackingId.value)).
          withRequestBody(equalTo(Json.toJson(request).toString())))
      }
    }
  }

  private def lookupService = new PaymentSolveWebServiceImpl(
    config = new TestConfig(paymentSolveMicroServiceUrlBase = s"http://localhost:$wireMockPort").build
  )

  private val trackingId =TrackingId("track-id-test")

  private def request = PaymentSolveBeginRequest(
    transactionId = "transaction id",
    transNo = "trans no",
    vrm = "reg number",
    purchaseAmount = 9999,
    paymentCallback = "callback url"
  )

  private implicit val paymentSolveBeginFormat = Json.format[PaymentSolveBeginRequest]
}
