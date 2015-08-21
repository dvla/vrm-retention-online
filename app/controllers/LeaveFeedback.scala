package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichResult
import utils.helpers.Config
import views.vrm_retention.RelatedCacheKeys

final class LeaveFeedback @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                      config: Config,
                                      dateService: common.services.DateService,
                                      surveyUrl: SurveyUrl) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.leave_feedback(surveyUrl()))
      .withNewSession
      .discardingCookies(RelatedCacheKeys.RetainSet)
  }
}

class SurveyUrl @Inject()(implicit config: Config) {
  def apply(): Option[String] = config.surveyUrl
}
