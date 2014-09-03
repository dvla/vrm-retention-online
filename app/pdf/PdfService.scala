package pdf

import scala.concurrent.Future
import viewmodels.EligibilityModel

trait PdfService {

  def create(implicit eligibilityModel: EligibilityModel, transactionId: String): Future[Array[Byte]]
}