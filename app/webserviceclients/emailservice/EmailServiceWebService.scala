package webserviceclients.emailservice

import play.api.libs.ws.WSResponse

import scala.concurrent.Future

trait EmailServiceWebService {
  def invoke(request: EmailServiceSendRequest): Future[WSResponse]
}