package services.vehicle_lookup

import com.google.inject.Inject
import common.LogFormats
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.{Response, WS}
import services.HttpHeaders
import utils.helpers.Config
import scala.concurrent.Future
import models.domain.common.VehicleDetailsRequest

final class VehicleLookupWebServiceImpl @Inject()(config: Config) extends VehicleLookupWebService {
  private val endPoint: String = s"${config.vehicleLookupMicroServiceBaseUrl}/vehicles/lookup/v1/dispose"

  override def callVehicleLookupService(request: VehicleDetailsRequest, trackingId: String): Future[Response] = {
    val vrm = LogFormats.anonymize(request.registrationNumber)
    val refNo = LogFormats.anonymize(request.referenceNumber)

    Logger.debug(s"Calling vehicle lookup micro-service with request $refNo $vrm")
    Logger.debug(s"Calling vehicle lookup micro-service with tracking id: $trackingId")
    WS.url(endPoint)
      .withHeaders(HttpHeaders.TrackingId -> trackingId)
      .post(Json.toJson(request))
  }
}