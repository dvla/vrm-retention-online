package composition

import play.api.mvc.RequestHeader
import uk.gov.dvla.vehicles.presentation.common.CommonGlobalSettings
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config

trait GlobalWithFilters extends CommonGlobalSettings with WithFilters {

  val serviceName = "vrm-retention-online"

  def viewNotFound(request: RequestHeader) = {
    implicit val config = injector.getInstance(classOf[Config])
    implicit val dateService = injector.getInstance(classOf[DateService])
    views.html.errors.onHandlerNotFound(request)
  }
}
