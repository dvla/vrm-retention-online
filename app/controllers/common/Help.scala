package controllers.common

import com.google.inject.Inject
import common.ClientSideSessionFactory
import common.CookieImplicits.{RichCookies, RichSimpleResult}
//import controllers.disposal_of_vehicle.routes.BeforeYouStart
import controllers.vrm_retention.routes.BeforeYouStart
import mappings.common.Help.HelpCacheKey
import play.api.mvc.{Action, Controller}
import utils.helpers.Config

final class Help @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                             config: Config) extends Controller {

  def present = Action { implicit request =>
    val origin = request.headers.get(REFERER).getOrElse("No Referer in header")
    Ok(views.html.common.help()).
      withCookie(HelpCacheKey, origin) // Save the previous page URL (from the referer header) into a cookie.
  }

  def back = Action { implicit request =>
    val origin: String = request.cookies.getString(HelpCacheKey).getOrElse(BeforeYouStart.present().url)
    Redirect(origin)
  }
}