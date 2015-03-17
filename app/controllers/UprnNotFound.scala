package controllers

import com.google.inject.Inject
import play.api.mvc.Action
import play.api.mvc.Controller
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

final class UprnNotFound @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                     config: Config) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.uprn_not_found())
  }
}