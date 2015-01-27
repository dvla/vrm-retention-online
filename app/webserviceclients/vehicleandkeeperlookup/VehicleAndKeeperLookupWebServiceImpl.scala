package webserviceclients.vehicleandkeeperlookup

import com.google.inject.Inject
import play.api.Logger
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.{WS, WSResponse}
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsRequest
import utils.helpers.{Config2, Config}
import scala.concurrent.Future

final class VehicleAndKeeperLookupWebServiceImpl @Inject()(
                                                            config: Config,
                                                            config2: Config2
                                                            )
  extends VehicleAndKeeperLookupWebService {

  private val endPoint: String =
    s"${config2.vehicleAndKeeperLookupMicroServiceBaseUrl}/vehicleandkeeper/lookup/v1"

  override def invoke(request: VehicleAndKeeperDetailsRequest, trackingId: String): Future[WSResponse] = {
    val vrm = LogFormats.anonymize(request.registrationNumber)
    val refNo = LogFormats.anonymize(request.referenceNumber)

    Logger.debug(s"Calling vehicle and keeper lookup micro-service with request $refNo $vrm tracking id: $trackingId")
    WS.url(endPoint)
      .withHeaders(HttpHeaders.TrackingId -> trackingId)
      .withRequestTimeout(config.vehicleAndKeeperLookupRequestTimeout) // Timeout is in milliseconds
      .post(Json.toJson(request))
  }
}