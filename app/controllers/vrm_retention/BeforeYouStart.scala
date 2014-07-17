package controllers.vrm_retention

import com.google.inject.Inject
import common.ClientSideSessionFactory
import common.CookieImplicits.RichSimpleResult
import mappings.vrm_retention.RelatedCacheKeys
import play.api.mvc._
import utils.helpers.Config

final class BeforeYouStart @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.before_you_start()).
      withNewSession.
      discardingCookies(RelatedCacheKeys.FullSet)
  }
}