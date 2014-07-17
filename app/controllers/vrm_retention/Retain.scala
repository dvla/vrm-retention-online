package controllers.vrm_retention

import com.google.inject.Inject
import common.{ClientSideSessionFactory, CookieImplicits}
import mappings.vrm_retention.RelatedCacheKeys
import play.api.mvc._
import utils.helpers.Config
import CookieImplicits.RichSimpleResult

final class Retain @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                             config: Config) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.retain()).
      withNewSession.
      discardingCookies(RelatedCacheKeys.FullSet)
  }
}