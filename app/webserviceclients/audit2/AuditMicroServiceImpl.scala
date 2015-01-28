package webserviceclients.audit2

import com.google.inject.Inject
import play.api.Logger
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.{WS, WSResponse}
import utils.helpers.{Config2, Config}

import scala.concurrent.Future

final class AuditMicroServiceImpl @Inject()(config: Config,
                                            config2: Config2) extends AuditMicroService {

  override def invoke(request: AuditRequest): Future[WSResponse] = {
    val endPoint: String = s"${config2.auditMicroServiceUrlBase}/audit/v1"
    val requestAsJson = Json.toJson(request)

    Logger.debug(s"Calling audit micro-service with request $request")
    WS.url(endPoint).
      withRequestTimeout(config2.auditMsRequestTimeout). // Timeout is in milliseconds
      post(requestAsJson)
  }
}