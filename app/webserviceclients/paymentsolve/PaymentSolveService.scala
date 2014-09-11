package webserviceclients.paymentsolve

import scala.concurrent.Future

trait PaymentSolveService {

  def invoke(cmd: PaymentSolveBeginRequest, trackingId: String): Future[PaymentSolveBeginResponse]
}