package controllers

import com.google.inject.Inject
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import utils.helpers.{Config, Config2}
import views.vrm_retention.RelatedCacheKeys

final class BeforeYouStart @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config,
                                       config2: Config2) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.before_you_start()).
      withNewSession.
      discardingCookies(RelatedCacheKeys.RetainSet)
  }
}