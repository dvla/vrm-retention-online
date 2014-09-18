package controllers

import com.google.inject.Inject
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import utils.helpers.Config
import views.vrm_retention.RelatedCacheKeys

final class MockFeedback @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                     config: Config) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.mock_gov_uk_feedback()).
      withNewSession.
      discardingCookies(RelatedCacheKeys.RetainSet)
  }
}