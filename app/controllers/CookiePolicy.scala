package controllers

import com.google.inject.Inject
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

final class CookiePolicy @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,

                                     config2: Config) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.cookie_policy())
  }
}