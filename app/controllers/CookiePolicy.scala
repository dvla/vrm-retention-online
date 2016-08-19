package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.model.CookieReport
import common.services.DateService
import utils.helpers.Config

final class CookiePolicy @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                     config: Config,
                                     dateService: DateService)
                                     extends uk.gov.dvla.vehicles.presentation.common.controllers.CookiePolicy {

  private val reports = cookies ++ List(
    CookieReport("tracking_id", "tracking_id", "normal", "7days"),
    CookieReport("PLAY_LANG", "PLAY_LANG", "session", "close"),
    CookieReport("40 character length", "multi-encrypted", "normal-secure", "7daysOr8hours"),
    CookieReport("liveagent_oref", "chat-support", "normal", "10years"),
    CookieReport("liveagent_vc", "chat-support", "normal", "10years"),
    CookieReport("liveagent_ptid", "chat-support", "normal", "10years"),
    CookieReport("liveagent_sid", "chat-support", "session", "close")
  )

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.cookie_policy(reports))
  }
}
