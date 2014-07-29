package services.vehicle_and_keeper_lookup

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, postRequestedFor, urlEqualTo}
import common.ClientSideSessionFactory
import helpers.{UnitSpec, WireMockFixture}
import models.domain.vrm_retention.VehicleAndKeeperDetailsRequest
import play.api.libs.json.Json
import services.HttpHeaders
import utils.helpers.Config

final class VehicleAndKeeperLookupWebServiceImplSpec extends UnitSpec with WireMockFixture {

  "callDisposeService" should {

    "send the serialised json request" in {
      val resultFuture = lookupService.callVehicleAndKeeperLookupService(request, trackingId)
      whenReady(resultFuture, timeout) { result =>
        wireMock.verifyThat(1, postRequestedFor(
          urlEqualTo(s"/vehicleandkeeper/lookup/v1")
        ).withHeader(HttpHeaders.TrackingId, equalTo(trackingId)).
          withRequestBody(equalTo(Json.toJson(request).toString())))
      }
    }
  }

  private implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
  private val lookupService = new VehicleAndKeeperLookupWebServiceImpl(new Config() {
    override val vehicleAndKeeperLookupMicroServiceBaseUrl = s"http://localhost:$wireMockPort"
  })

  private final val trackingId = "track-id-test"

  private val request = VehicleAndKeeperDetailsRequest(
    referenceNumber = "ref number",
    registrationNumber = "reg number"
  )

  private implicit val vehicleAndKeeperDetailsFormat = Json.format[VehicleAndKeeperDetailsRequest]
}