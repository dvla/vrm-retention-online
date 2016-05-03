package webserviceclients.audit2

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import composition.TestConfig
import helpers.TestWithApplication
import helpers.WireMockFixture
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.testhelpers.IntegrationTestHelper

class AuditMicroServiceImplSpec extends IntegrationTestHelper with WireMockFixture {

  "invoke" should {
    "send the serialised json request" in new TestWithApplication {
      val resultFuture = auditMicroService.invoke(request, TrackingId("testTrackingId"))
      whenReady(resultFuture) { result =>
        wireMock.verifyThat(1, postRequestedFor(
          urlEqualTo(s"/audit/v1")
        ).withRequestBody(equalTo(Json.toJson(request).toString())))
      }
    }
  }

  private def auditMicroService = new AuditMicroServiceImpl(
    config = new TestConfig(auditMicroServiceUrlBase = s"http://localhost:$wireMockPort").build
  )

  private def request = {
    val data: Seq[(String, Any)] = Seq(("stub-key", "stub-value"))
    AuditRequest(
      name = "transaction id",
      serviceType = "trans no",
      data = data
    )
  }
}
