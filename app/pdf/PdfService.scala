package pdf

import models.domain.common.VehicleDetailsModel
import models.domain.vrm_retention.RetainModel
import scala.concurrent.Future

trait PdfService {

  def create(implicit vehicleDetails: VehicleDetailsModel, retainModel: RetainModel): Future[Array[Byte]]
}