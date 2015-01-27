package webserviceclients.paymentsolve

import com.google.inject.Inject
import play.api.Logger
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.{WS, WSResponse}
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders
import utils.helpers.{Config2, Config}
import scala.concurrent.Future

final class PaymentSolveWebServiceImpl @Inject()(
                                                  config: Config,
                                                  config2: Config2
                                                  ) extends PaymentSolveWebService {

  override def invoke(request: PaymentSolveBeginRequest, trackingId: String): Future[WSResponse] = {
    val vrm = LogFormats.anonymize(request.vrm)
    val endPoint: String = s"${config.paymentSolveMicroServiceUrlBase}/payment/solve/beginWebPayment"

    Logger.debug(endPoint)
    Logger.debug(s"Calling payment solve micro-service with request ${request.transNo} and $vrm")
    WS.url(endPoint).
      withHeaders(HttpHeaders.TrackingId -> trackingId).
      withRequestTimeout(config.paymentSolveMsRequestTimeout). // Timeout is in milliseconds
      post(Json.toJson(request))
  }

  override def invoke(request: PaymentSolveGetRequest, trackingId: String): Future[WSResponse] = {
    val trxRef = LogFormats.anonymize(request.trxRef)
    val endPoint: String = s"${config.paymentSolveMicroServiceUrlBase}/payment/solve/getWebPayment"

    Logger.debug(endPoint)
    Logger.debug(s"Calling payment solve micro-service with request $trxRef")
    WS.url(endPoint).
      withHeaders(HttpHeaders.TrackingId -> trackingId).
      withRequestTimeout(config.paymentSolveMsRequestTimeout). // Timeout is in milliseconds
      post(Json.toJson(request))
  }

  override def invoke(request: PaymentSolveCancelRequest, trackingId: String): Future[WSResponse] = {
    val trxRef = LogFormats.anonymize(request.trxRef)
    val endPoint: String = s"${config.paymentSolveMicroServiceUrlBase}/payment/solve/cancelWebPayment"

    Logger.debug(endPoint)
    Logger.debug(s"Calling payment solve micro-service with request $trxRef")
    WS.url(endPoint).
      withHeaders(HttpHeaders.TrackingId -> trackingId).
      withRequestTimeout(config.paymentSolveMsRequestTimeout). // Timeout is in milliseconds
      post(Json.toJson(request))
  }

  override def invoke(request: PaymentSolveUpdateRequest, trackingId: String): Future[WSResponse] = {
    val trxRef = LogFormats.anonymize(request.trxRef)
    val endPoint: String = s"${config.paymentSolveMicroServiceUrlBase}/payment/solve/updateWebPayment"

    Logger.debug(endPoint)
    Logger.debug(s"Calling payment solve micro-service with request $trxRef")
    WS.url(endPoint).
      withHeaders(HttpHeaders.TrackingId -> trackingId).
      withRequestTimeout(config.paymentSolveMsRequestTimeout). // Timeout is in milliseconds
      post(Json.toJson(request))
  }
}