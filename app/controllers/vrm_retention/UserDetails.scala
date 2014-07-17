package controllers.vrm_retention

import play.api.mvc._
import com.google.inject.Inject
import common.{ClientSideSessionFactory, CookieImplicits}
import CookieImplicits.RichSimpleResult
import CookieImplicits.RichCookies
import CookieImplicits.RichForm
import mappings.vrm_retention.RelatedCacheKeys
import play.api.Play.current
import utils.helpers.Config

final class UserDetails @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                  config: Config) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.user_details()).
      withNewSession.
      discardingCookies(RelatedCacheKeys.FullSet)
  }
}