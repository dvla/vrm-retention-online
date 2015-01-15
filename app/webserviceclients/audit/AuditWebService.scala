package webserviceclients.audit

import play.api.libs.ws.WSResponse

import scala.concurrent.Future

trait AuditWebService {

  def invoke(request: AuditRequest): Future[WSResponse]
}