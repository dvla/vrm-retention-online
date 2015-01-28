package webserviceclients.paymentsolve

import javax.inject.Inject

import play.api.http.Status

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

final class PaymentSolveServiceImpl @Inject()(ws: PaymentSolveWebService)
  extends PaymentSolveService {

  override def invoke(cmd: PaymentSolveBeginRequest,
                      trackingId: String): Future[PaymentSolveBeginResponse] = {
    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK) resp.json.as[PaymentSolveBeginResponse]
      else throw new RuntimeException(
        s"Payment Solve web service call http status not OK, it " +
          s"was: ${resp.status}. Problem may come from either payment-solve micro-service or Solve"
      )
    }.recover {
      case NonFatal(e) => throw new RuntimeException("Payment Solve call failed for an unknown reason", e)
    }
  }

  override def invoke(cmd: PaymentSolveGetRequest,
                      trackingId: String): Future[PaymentSolveGetResponse] = {
    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK) resp.json.as[PaymentSolveGetResponse]
      else throw new RuntimeException(
        s"Payment Solve web service call http status not OK, it " +
          s"was: ${resp.status}. Problem may come from either payment-solve micro-service or Solve"
      )
    }.recover {
      case NonFatal(e) => throw new RuntimeException("Payment Solve call failed for an unknown reason", e)
    }
  }

  override def invoke(cmd: PaymentSolveCancelRequest,
                      trackingId: String): Future[PaymentSolveCancelResponse] = {
    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK) resp.json.as[PaymentSolveCancelResponse]
      else throw new RuntimeException(
        s"Payment Solve web service call http status not OK, it " +
          s"was: ${resp.status}. Problem may come from either payment-solve micro-service or Solve"
      )
    }.recover {
      case NonFatal(e) => throw new RuntimeException("Payment Solve call failed for an unknown reason", e)
    }
  }

  override def invoke(cmd: PaymentSolveUpdateRequest,
                      trackingId: String): Future[PaymentSolveUpdateResponse] = {
    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK) resp.json.as[PaymentSolveUpdateResponse]
      else throw new RuntimeException(
        s"Payment Solve web service call http status not OK, it " +
          s"was: ${resp.status}. Problem may come from either payment-solve micro-service or Solve"
      )
    }.recover {
      case NonFatal(e) => throw new RuntimeException("Payment Solve call failed for an unknown reason", e)
    }
  }
}