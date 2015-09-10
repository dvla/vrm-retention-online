package webserviceclients.audit2

import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.libs.ws.WS
import play.api.libs.ws.WSResponse
import play.api.Play.current
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders
import utils.helpers.Config

final class AuditMicroServiceImpl @Inject()(config: Config) extends AuditMicroService {

  override def invoke(request: AuditRequest, trackingId: TrackingId): Future[WSResponse] = {
    val endPoint: String = s"${config.auditMicroServiceUrlBase}/audit/v1"
    val requestAsJson = Json.toJson(request)

    WS.url(endPoint)
      .withHeaders(HttpHeaders.TrackingId -> trackingId.value)
      .withRequestTimeout(config.auditMsRequestTimeout) // Timeout is in milliseconds
      .post(requestAsJson)
  }
}