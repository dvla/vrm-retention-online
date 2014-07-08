package services.dispose_service

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, postRequestedFor, urlEqualTo}
import common.ClientSideSessionFactory
import helpers.{UnitSpec, WireMockFixture}
import models.domain.disposal_of_vehicle.{DisposalAddressDto, DisposeRequest}
import play.api.libs.json.Json
import services.HttpHeaders
import utils.helpers.Config

class DisposeWebServiceImplSpec extends UnitSpec with WireMockFixture {

  implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
  val disposeService = new DisposeWebServiceImpl(new Config() {
    override val disposeVehicleMicroServiceBaseUrl = s"http://localhost:$wireMockPort"
  })

  private final val trackingId = "track-id-test"

  implicit val disposalAddressDtoFormat = Json.format[DisposalAddressDto]
  implicit val disposeRequestFormat = Json.format[DisposeRequest]
  val request = DisposeRequest(
    referenceNumber = "ref number",
    registrationNumber = "reg number",
    traderName = "trader test",
    traderAddress = DisposalAddressDto(
      line = Seq("line1", "line2"),
      postTown = Some("town"),
      postCode = "W193NE",
      uprn = Some(3123L)
    ),
    dateOfDisposal = "",
    transactionTimestamp = "",
    prConsent = true,
    keeperConsent = false,
    mileage = Some(12)
  )

  "callDisposeService" should {
    "send the serialised json request" in {
      val resultFuture = disposeService.callDisposeService(request, trackingId)
      whenReady(resultFuture, timeout) { result =>
        wireMock.verifyThat(1, postRequestedFor(
          urlEqualTo(s"/vehicles/dispose/v1")
        ).withHeader(HttpHeaders.TrackingId, equalTo(trackingId)).
          withRequestBody(equalTo(Json.toJson(request).toString())))
      }
    }
  }
}