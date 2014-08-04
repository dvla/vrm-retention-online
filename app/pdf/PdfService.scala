package pdf

import models.domain.vrm_retention.VehicleAndKeeperDetailsModel
import models.domain.vrm_retention.RetainModel
import scala.concurrent.Future

trait PdfService {

  def create(implicit vehicleDetails: VehicleAndKeeperDetailsModel, retainModel: RetainModel): Future[Array[Byte]]
}