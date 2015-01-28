package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config2

final class UprnNotFound @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,

                                     config2: Config2) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.uprn_not_found())
  }
}