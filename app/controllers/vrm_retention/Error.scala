package controllers.vrm_retention

import com.google.inject.Inject
import common.ClientSideSessionFactory
import play.api.Logger
import play.api.mvc.{Action, Controller}
import utils.helpers.{Config, CryptoHelper}

final class Error @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {

  def present(exceptionDigest: String) = Action { implicit request =>
    Logger.debug("Error - Displaying generic error page")
    Ok(views.html.vrm_retention.error(exceptionDigest))
  }

  // TODO is there a submit button that calls this? If it is unused then delete.
  def submit(exceptionDigest: String) = Action.async { implicit request =>
    Logger.debug("Error submit called - now removing full set of cookies and redirecting to Start page")
    CryptoHelper.discardAllCookies
  }
}