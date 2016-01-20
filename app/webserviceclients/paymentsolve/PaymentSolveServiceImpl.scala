package webserviceclients.paymentsolve

import javax.inject.Inject
import PaymentSolveServiceImpl.ServiceName
import play.api.http.Status
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStats
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStatsFailure
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStatsSuccess

final class PaymentSolveServiceImpl @Inject()(ws: PaymentSolveWebService,
                                              dateService: DateService,
                                              healthStats: HealthStats) extends PaymentSolveService {

  override def invoke(cmd: PaymentSolveBeginRequest,
                      trackingId: TrackingId): Future[(Int, PaymentSolveBeginResponse)] = {
    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK) {
        healthStats.success(HealthStatsSuccess(ServiceName, dateService.now))
        (resp.status, resp.json.as[PaymentSolveBeginResponse])
      }
      else if (resp.status == Status.INTERNAL_SERVER_ERROR) {
        val msg = s"Payment Solve micro-service call http status not OK, it was: ${resp.status}"
        val error = new RuntimeException(msg)
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, error))
        (resp.status, resp.json.as[PaymentSolveBeginResponse])
      }
      else {
        val error = new RuntimeException(
          "Payment Solve web service call http status not OK, it " +
            s"was: ${resp.status}. Problem may come from either payment-solve micro-service or Solve"
        )
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, error))
        throw error
      }
    }.recover {
      case NonFatal(e) =>
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, e))
        throw e
    }
  }

  override def invoke(cmd: PaymentSolveGetRequest,
                      trackingId: TrackingId): Future[(Int, PaymentSolveGetResponse)] = {
    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK) {
        healthStats.success(HealthStatsSuccess(ServiceName, dateService.now))
        (resp.status, resp.json.as[PaymentSolveGetResponse])
      }
      else if (resp.status == Status.INTERNAL_SERVER_ERROR) {
        val msg = s"Payment Solve micro-service call http status not OK, it was: ${resp.status}"
        val error = new RuntimeException(msg)
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, error))
        (resp.status, resp.json.as[PaymentSolveGetResponse])
      }
      else {
        val error = new RuntimeException(
          "Payment Solve web service call http status not OK, it " +
            s"was: ${resp.status}. Problem may come from either payment-solve micro-service or Solve"
        )
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, error))
        throw error
      }
    }.recover {
      case NonFatal(e) =>
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, e))
        throw e
    }
  }

  override def invoke(cmd: PaymentSolveCancelRequest,
                      trackingId: TrackingId): Future[(Int, PaymentSolveCancelResponse)] = {
    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK) {
        healthStats.success(HealthStatsSuccess(ServiceName, dateService.now))
        (resp.status, resp.json.as[PaymentSolveCancelResponse])
      }
      else if (resp.status == Status.INTERNAL_SERVER_ERROR) {
        val msg = s"Payment Solve micro-service call http status not OK, it was: ${resp.status}"
        val error = new RuntimeException(msg)
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, error))
        (resp.status, resp.json.as[PaymentSolveCancelResponse])
      }
      else {
        val error =new RuntimeException(
          "Payment Solve web service call http status not OK, it " +
            s"was: ${resp.status}. Problem may come from either payment-solve micro-service or Solve"
        )
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, error))
        throw error
      }
    }.recover {
      case NonFatal(e) =>
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, e))
        throw e
    }
  }

  override def invoke(cmd: PaymentSolveUpdateRequest,
                      trackingId: TrackingId): Future[(Int, PaymentSolveUpdateResponse)] = {
    ws.invoke(cmd, trackingId).map { resp =>
      if (resp.status == Status.OK) {
        healthStats.success(HealthStatsSuccess(ServiceName, dateService.now))
        (resp.status, resp.json.as[PaymentSolveUpdateResponse])
      }
      else if (resp.status == Status.INTERNAL_SERVER_ERROR) {
        val msg = s"Payment Solve micro-service call http status not OK, it was: ${resp.status}"
        val error = new RuntimeException(msg)
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, error))
        (resp.status, resp.json.as[PaymentSolveUpdateResponse])
      }
      else {
        val error = new RuntimeException(
          "Payment Solve web service call http status not OK, it " +
            s"was: ${resp.status}. Problem may come from either payment-solve micro-service or Solve"
        )
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, error))
        throw error
      }
    }.recover {
      case NonFatal(e) =>
        healthStats.failure(HealthStatsFailure(ServiceName, dateService.now, e))
        throw e
    }
  }
}

object PaymentSolveServiceImpl {
  final val ServiceName = "payment-solve-microservice"
}