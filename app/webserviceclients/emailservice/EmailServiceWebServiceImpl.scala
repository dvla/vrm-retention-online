package webserviceclients.emailservice

import com.google.inject.Inject
import play.api.Logger
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.{WS, WSResponse}
import utils.helpers.Config

import scala.concurrent.Future

final class EmailServiceWebServiceImpl @Inject()(config: Config) extends EmailServiceWebService {

  override def invoke(request: EmailServiceSendRequest): Future[WSResponse] = {

    val endPoint: String = s"${config.emailServiceMicroServiceUrlBase}/email/send"

    Logger.debug(endPoint)
    Logger.debug(s"Calling email service micro-service with request")
    WS.url(endPoint).
      withRequestTimeout(config.emailServiceMsRequestTimeout). // Timeout is in milliseconds
      post(Json.toJson(request))
  }

}