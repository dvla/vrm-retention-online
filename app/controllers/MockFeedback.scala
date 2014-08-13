package controllers

import com.google.inject.Inject
import mappings.vrm_retention.RelatedCacheKeys
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichSimpleResult
import utils.helpers.Config

final class MockFeedback @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.mock_gov_uk_feedback()).
      withNewSession.
      discardingCookies(RelatedCacheKeys.FullSet)
  }

}