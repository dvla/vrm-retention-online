package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import utils.helpers.Config
import views.vrm_retention.RelatedCacheKeys

final class BeforeYouStart @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config,
                                       dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.before_you_start())
      .withNewSession
      .discardingCookies(RelatedCacheKeys.RetainSet)
  }
}