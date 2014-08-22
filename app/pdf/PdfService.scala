package pdf

import viewmodels.{RetainModel, VehicleAndKeeperDetailsModel}
import scala.concurrent.Future

trait PdfService {

  def create(implicit vehicleDetails: VehicleAndKeeperDetailsModel, transactionId: String): Future[Array[Byte]]
}