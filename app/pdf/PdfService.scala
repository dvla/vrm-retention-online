package pdf

import models.domain.common.VehicleDetailsModel
import models.domain.vrm_retention.{KeeperDetailsModel, VehicleLookupFormModel}
import org.apache.pdfbox.pdmodel.PDDocument
import scala.concurrent.Future

trait PdfService {

  def create(vehicleDetails: VehicleDetailsModel,
             keeperDetails: KeeperDetailsModel,
             vehicleLookupFormModel: VehicleLookupFormModel): Future[PDDocument]
}