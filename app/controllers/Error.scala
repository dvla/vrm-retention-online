package controllers

import com.google.inject.Inject
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.{Config, CookieHelper}

final class Error @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,

                              config2: Config) extends Controller {

  def present(exceptionDigest: String) = Action { implicit request =>
    Logger.debug("Error - Displaying generic error page")
    Ok(views.html.vrm_retention.error(exceptionDigest))
  }

  def startAgain(exceptionDigest: String) = Action.async { implicit request =>
    Logger.debug("Error start again called - now removing full set of cookies and redirecting to Start page")
    CookieHelper.discardAllCookies
  }
}