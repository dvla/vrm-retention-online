package webserviceclients.paymentsolve

import play.api.libs.ws.WSResponse
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

import scala.concurrent.Future

trait PaymentSolveWebService {

  def invoke(request: PaymentSolveBeginRequest, tracking: TrackingId): Future[WSResponse]

  def invoke(request: PaymentSolveGetRequest, tracking: TrackingId): Future[WSResponse]

  def invoke(request: PaymentSolveCancelRequest, tracking: TrackingId): Future[WSResponse]

  def invoke(request: PaymentSolveUpdateRequest, tracking: TrackingId): Future[WSResponse]
}