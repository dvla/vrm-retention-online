package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config2

final class MicroServiceError @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,

                                        config2: Config2) extends Controller {

  def present = Action { implicit request =>
    ServiceUnavailable(views.html.vrm_retention.micro_service_error())
  }
}
