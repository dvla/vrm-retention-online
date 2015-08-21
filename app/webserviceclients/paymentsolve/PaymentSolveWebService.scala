package webserviceclients.paymentsolve

import play.api.libs.ws.WSResponse
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

trait PaymentSolveWebService {

  def invoke(request: PaymentSolveBeginRequest, tracking: TrackingId): Future[WSResponse]

  def invoke(request: PaymentSolveGetRequest, tracking: TrackingId): Future[WSResponse]

  def invoke(request: PaymentSolveCancelRequest, tracking: TrackingId): Future[WSResponse]

  def invoke(request: PaymentSolveUpdateRequest, tracking: TrackingId): Future[WSResponse]
}