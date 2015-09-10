package webserviceclients.paymentsolve

import com.google.inject.Inject
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.WS
import play.api.libs.ws.WSResponse
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.LogFormats.anonymize
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders
import utils.helpers.Config

final class PaymentSolveWebServiceImpl @Inject()(config: Config) extends PaymentSolveWebService with DVLALogger {

  override def invoke(request: PaymentSolveBeginRequest, trackingId: TrackingId): Future[WSResponse] = {
    val vrm = anonymize(request.vrm)
    val endPoint: String = s"${config.paymentSolveMicroServiceUrlBase}/payment/solve/beginWebPayment"

    logMessage(trackingId, Info, s"Calling payment solve micro-service with request ${request.transNo} and $vrm")
    WS.url(endPoint)
      .withHeaders(HttpHeaders.TrackingId -> trackingId.value)
      .withRequestTimeout(config.paymentSolveMsRequestTimeout) // Timeout is in milliseconds
      .post(Json.toJson(request))
  }

  override def invoke(request: PaymentSolveGetRequest, trackingId: TrackingId): Future[WSResponse] = {
    val trxRef = anonymize(request.trxRef)
    val endPoint: String = s"${config.paymentSolveMicroServiceUrlBase}/payment/solve/getWebPayment"

    logMessage(trackingId, Info, s"Calling payment solve micro-service with request $trxRef")
    WS.url(endPoint)
      .withHeaders(HttpHeaders.TrackingId -> trackingId.value)
      .withRequestTimeout(config.paymentSolveMsRequestTimeout) // Timeout is in milliseconds
      .post(Json.toJson(request))
  }

  override def invoke(request: PaymentSolveCancelRequest, trackingId: TrackingId): Future[WSResponse] = {
    val trxRef = anonymize(request.trxRef)
    val endPoint: String = s"${config.paymentSolveMicroServiceUrlBase}/payment/solve/cancelWebPayment"

    logMessage(trackingId, Info, s"Calling payment solve micro-service with request $trxRef")
    WS.url(endPoint)
      .withHeaders(HttpHeaders.TrackingId -> trackingId.value)
      .withRequestTimeout(config.paymentSolveMsRequestTimeout) // Timeout is in milliseconds
      .post(Json.toJson(request))
  }

  override def invoke(request: PaymentSolveUpdateRequest, trackingId: TrackingId): Future[WSResponse] = {
    val trxRef = anonymize(request.trxRef)
    val endPoint: String = s"${config.paymentSolveMicroServiceUrlBase}/payment/solve/updateWebPayment"

    logMessage(trackingId, Info, s"Calling payment solve micro-service with request $trxRef")
    WS.url(endPoint)
      .withHeaders(HttpHeaders.TrackingId -> trackingId.value)
      .withRequestTimeout(config.paymentSolveMsRequestTimeout) // Timeout is in milliseconds
      .post(Json.toJson(request))
  }
}