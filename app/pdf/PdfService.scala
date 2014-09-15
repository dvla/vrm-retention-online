package pdf

import viewmodels.EligibilityModel
import scala.concurrent.Future

trait PdfService {

  def create(implicit eligibilityModel: EligibilityModel, transactionId: String): Future[Array[Byte]]
}