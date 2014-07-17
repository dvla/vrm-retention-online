package services.vehicle_lookup

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, postRequestedFor, urlEqualTo}
import common.ClientSideSessionFactory
import helpers.{UnitSpec, WireMockFixture}
import models.domain.common.VehicleDetailsRequest
import play.api.libs.json.Json
import services.HttpHeaders
import utils.helpers.Config

final class VehicleLookupWebServiceImplSpec extends UnitSpec with WireMockFixture {

  "callDisposeService" should {

    "send the serialised json request" in {
      val resultFuture = lookupService.callVehicleLookupService(request, trackingId)
      whenReady(resultFuture, timeout) { result =>
        wireMock.verifyThat(1, postRequestedFor(
          urlEqualTo(s"/vehicles/lookup/v1/dispose")
        ).withHeader(HttpHeaders.TrackingId, equalTo(trackingId)).
          withRequestBody(equalTo(Json.toJson(request).toString())))
      }
    }
  }

  private implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
  private val lookupService = new VehicleLookupWebServiceImpl(new Config() {
    override val vehicleLookupMicroServiceBaseUrl = s"http://localhost:$wireMockPort"
  })

  private final val trackingId = "track-id-test"

  private val request = VehicleDetailsRequest(
    referenceNumber = "ref number",
    registrationNumber = "reg number",
    userName = "user name"
  )

  private implicit val vehiclesDetailsFormat = Json.format[VehicleDetailsRequest]
}