package controllers.vrm_retention

import play.api.mvc._
import com.google.inject.Inject
import common.{ClientSideSessionFactory, CookieImplicits}
import CookieImplicits.RichSimpleResult
import CookieImplicits.RichCookies
import mappings.vrm_retention.RelatedCacheKeys
import play.api.Play.current
import utils.helpers.Config

final class Retain @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory, config: Config) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.retain()).
      withNewSession.
      discardingCookies(RelatedCacheKeys.FullSet)
  }

}
