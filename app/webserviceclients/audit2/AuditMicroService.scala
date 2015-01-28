package webserviceclients.audit2

import play.api.libs.ws.WSResponse

import scala.concurrent.Future

trait AuditMicroService {

  def invoke(request: AuditRequest): Future[WSResponse]
}