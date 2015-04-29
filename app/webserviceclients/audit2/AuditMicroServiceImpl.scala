package webserviceclients.audit2

import com.google.inject.Inject
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.WS
import play.api.libs.ws.WSResponse
import utils.helpers.Config

import scala.concurrent.Future

final class AuditMicroServiceImpl @Inject()(
                                             config: Config) extends AuditMicroService {

  override def invoke(request: AuditRequest): Future[WSResponse] = {
    val endPoint: String = s"${config.auditMicroServiceUrlBase}/audit/v1"
    val requestAsJson = Json.toJson(request)

    WS.url(endPoint).
      withRequestTimeout(config.auditMsRequestTimeout). // Timeout is in milliseconds
      post(requestAsJson)
  }
}