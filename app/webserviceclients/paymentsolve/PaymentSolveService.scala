package webserviceclients.paymentsolve

import scala.concurrent.Future

trait PaymentSolveService {

  def invoke(cmd: PaymentSolveBeginRequest, trackingId: String): Future[PaymentSolveBeginResponse]
  def invoke(cmd: PaymentSolveGetRequest, trackingId: String): Future[PaymentSolveGetResponse]
}