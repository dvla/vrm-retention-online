package webserviceclients.audit2

import com.google.inject.Inject
import play.api.Logger
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.{WS, WSResponse}
import utils.helpers.Config

import scala.concurrent.Future

final class AuditMicroServiceImpl @Inject()(config: Config) extends AuditMicroService {

  override def invoke(request: AuditRequest): Future[WSResponse] = {
    val endPoint: String = s"${config.auditMicroServiceUrlBase}/audit/v1"
    val requestAsJson = Json.toJson(request)

    Logger.debug(s"Calling audit micro-service with request $request")
    WS.url(endPoint).
      withRequestTimeout(config.auditMsRequestTimeout). // Timeout is in milliseconds
      post(requestAsJson)
  }
}