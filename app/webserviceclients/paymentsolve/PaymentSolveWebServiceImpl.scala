package webserviceclients.paymentsolve

import com.google.inject.Inject
import play.api.Logger
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.{WS, WSResponse}
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders
import utils.helpers.Config
import scala.concurrent.Future

final class PaymentSolveWebServiceImpl @Inject()(config: Config) extends PaymentSolveWebService {

  private val endPoint: String = s"${config.paymentSolveMicroServiceUrlBase}/payment/solve/beginWebPayment"

  override def invoke(request: PaymentSolveBeginRequest, trackingId: String): Future[WSResponse] = {
    val vrm = LogFormats.anonymize(request.vrm)

    Logger.debug(endPoint)
    Logger.debug(s"Calling payment solve micro-service with request ${request.transNo} and $vrm")
    WS.url(endPoint).
      withHeaders(HttpHeaders.TrackingId -> trackingId).
      post(Json.toJson(request))
  }
}