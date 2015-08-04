package pdf

import models.EligibilityModel
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel

import scala.concurrent.Future

trait PdfService extends DVLALogger {

  def create(eligibilityModel: EligibilityModel,
             transactionId: String,
             name: String,
             address: Option[AddressModel], trackingId: TrackingId): Array[Byte]
}