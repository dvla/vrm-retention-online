package services.dispose_service

import javax.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import models.domain.disposal_of_vehicle.{DisposeRequest, DisposeResponse}
import play.api.Logger
import play.api.http.Status.OK
import utils.helpers.Config
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class DisposeServiceImpl @Inject()(config: Config, ws: DisposeWebService) extends DisposeService {

  override def invoke(cmd: DisposeRequest, trackingId: String): Future[(Int, Option[DisposeResponse])] = {
    val vrm = LogFormats.anonymize(cmd.registrationNumber)
    val refNo = LogFormats.anonymize(cmd.referenceNumber)
    val postcode = LogFormats.anonymize(cmd.traderAddress.postCode)

    Logger.debug("Calling dispose vehicle micro-service with " +
      s"$refNo $vrm $postcode ${cmd.keeperConsent} ${cmd.prConsent} ${cmd.mileage}")

    ws.callDisposeService(cmd, trackingId).map { resp =>
      Logger.debug(s"Http response code from dispose vehicle micro-service was: ${resp.status}")

      if (resp.status == OK) (resp.status, resp.json.asOpt[DisposeResponse])
      else (resp.status, None)
    }
  }
}