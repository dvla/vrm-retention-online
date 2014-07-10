package services.vehicle_lookup

import com.github.tomakehurst.wiremock.client.WireMock.{postRequestedFor, urlEqualTo, equalTo}
import common.ClientSideSessionFactory
import helpers.{UnitSpec, WireMockFixture}
import models.domain.disposal_of_vehicle.VehicleDetailsRequest
import play.api.libs.json.Json
import services.HttpHeaders
import utils.helpers.Config

class VehicleLookupWebServiceImplSpec  extends UnitSpec  with WireMockFixture {

  implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
  val lookupService = new VehicleLookupWebServiceImpl(new Config() {
    override val vehicleLookupMicroServiceBaseUrl = s"http://localhost:$wireMockPort"
  })

  val trackingId = "track-id-test"

  val request = VehicleDetailsRequest(
    referenceNumber = "ref number",
    registrationNumber = "reg number",
    userName = "user name"
  )

  implicit val vehiclesDetailsFormat = Json.format[VehicleDetailsRequest]

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
}