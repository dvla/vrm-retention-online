package webserviceclients.paymentsolve

import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

import scala.concurrent.Future

trait PaymentSolveService {

  def invoke(cmd: PaymentSolveBeginRequest, trackingId: TrackingId): Future[PaymentSolveBeginResponse]

  def invoke(cmd: PaymentSolveGetRequest, trackingId: TrackingId): Future[PaymentSolveGetResponse]

  def invoke(cmd: PaymentSolveCancelRequest, trackingId: TrackingId): Future[PaymentSolveCancelResponse]

  def invoke(cmd: PaymentSolveUpdateRequest, trackingId: TrackingId): Future[PaymentSolveUpdateResponse]
}