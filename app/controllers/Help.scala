package controllers

import com.google.inject.Inject
import controllers.routes.BeforeYouStart
import mappings.common.Help.HelpCacheKey
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import utils.helpers.Config

final class Help @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                             config: Config) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.common.help()) // TODO revisit persisting of 'origin' cookie.
  }

  def back = Action { implicit request =>
    val origin: String = request.cookies.getString(HelpCacheKey).getOrElse(BeforeYouStart.present().url)
    Redirect(origin)
  }
}
