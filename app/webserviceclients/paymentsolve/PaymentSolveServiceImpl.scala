package webserviceclients.paymentsolve

import javax.inject.Inject
import play.api.http.Status
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

final class PaymentSolveServiceImpl @Inject()(ws: PaymentSolveWebService) extends PaymentSolveService {

  override def invoke(cmd: PaymentSolveBeginRequest,
                      trackingId: TrackingId): Future[(Int, PaymentSolveBeginResponse)] = {
    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK) (resp.status, resp.json.as[PaymentSolveBeginResponse])
      else if (resp.status == Status.INTERNAL_SERVER_ERROR) (resp.status, resp.json.as[PaymentSolveBeginResponse])
      else throw new RuntimeException(
        "Payment Solve web service call http status not OK, it " +
          s"was: ${resp.status}. Problem may come from either payment-solve micro-service or Solve"
      )
    }.recover {
      case NonFatal(e) => throw e
    }
  }

  override def invoke(cmd: PaymentSolveGetRequest,
                      trackingId: TrackingId): Future[(Int, PaymentSolveGetResponse)] = {
    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK) (resp.status, resp.json.as[PaymentSolveGetResponse])
      else if (resp.status == Status.INTERNAL_SERVER_ERROR) (resp.status, resp.json.as[PaymentSolveGetResponse])
      else throw new RuntimeException(
        "Payment Solve web service call http status not OK, it " +
          s"was: ${resp.status}. Problem may come from either payment-solve micro-service or Solve"
      )
    }.recover {
      case NonFatal(e) => throw e
    }
  }

  override def invoke(cmd: PaymentSolveCancelRequest,
                      trackingId: TrackingId): Future[(Int, PaymentSolveCancelResponse)] = {
    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK) (resp.status, resp.json.as[PaymentSolveCancelResponse])
      else if (resp.status == Status.INTERNAL_SERVER_ERROR) (resp.status, resp.json.as[PaymentSolveCancelResponse])
      else throw new RuntimeException(
        "Payment Solve web service call http status not OK, it " +
          s"was: ${resp.status}. Problem may come from either payment-solve micro-service or Solve"
      )
    }.recover {
      case NonFatal(e) => throw e
    }
  }

  override def invoke(cmd: PaymentSolveUpdateRequest,
                      trackingId: TrackingId): Future[(Int, PaymentSolveUpdateResponse)] = {
    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK) (resp.status, resp.json.as[PaymentSolveUpdateResponse])
      else if (resp.status == Status.INTERNAL_SERVER_ERROR) (resp.status, resp.json.as[PaymentSolveUpdateResponse])
      else throw new RuntimeException(
        "Payment Solve web service call http status not OK, it " +
          s"was: ${resp.status}. Problem may come from either payment-solve micro-service or Solve"
      )
    }.recover {
      case NonFatal(e) => throw e
    }
  }
}
