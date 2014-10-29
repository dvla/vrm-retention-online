package webserviceclients.paymentsolve

import play.api.libs.ws.WSResponse
import scala.concurrent.Future

trait PaymentSolveWebService {

  def invoke(request: PaymentSolveBeginRequest, tracking: String): Future[WSResponse]

  def invoke(request: PaymentSolveGetRequest, tracking: String): Future[WSResponse]

  def invoke(request: PaymentSolveCancelRequest, tracking: String): Future[WSResponse]

  def invoke(request: PaymentSolveUpdateRequest, tracking: String): Future[WSResponse]
}