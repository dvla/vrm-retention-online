package controllers

import com.google.inject.Inject
import play.api.mvc.Action
import play.api.mvc.Controller
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import utils.helpers.Config
import utils.helpers.CookieHelper
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies

final class Error @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config,
                              dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService
                             ) extends Controller with DVLALogger {

  def present(exceptionDigest: String) = Action { implicit request =>
    logMessage(request.cookies.trackingId, Error, "Displaying generic error page")
    Ok(views.html.vrm_retention.error(exceptionDigest))
  }

  def startAgain(exceptionDigest: String) = Action { implicit request =>
    logMessage(
      request.cookies.trackingId,
      Error,
      "Start again called - now removing full set of cookies and redirecting to Start page"
    )
    CookieHelper.discardAllCookies
  }
}