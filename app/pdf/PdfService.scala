package pdf

import models.EligibilityModel
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import scala.concurrent.Future

trait PdfService {

  def create(eligibilityModel: EligibilityModel, transactionId: String, name: String, address: Option[AddressModel]): Future[Array[Byte]]
}