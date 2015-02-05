package controllers

import com.google.inject.Inject
import play.api.mvc.Action
import play.api.mvc.Controller
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

final class MicroServiceError @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,

                                        config2: Config) extends Controller {

  def present = Action { implicit request =>
    ServiceUnavailable(views.html.vrm_retention.micro_service_error())
  }
}
