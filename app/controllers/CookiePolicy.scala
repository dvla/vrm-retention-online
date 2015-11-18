package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config

final class CookiePolicy @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                     config: Config,
                                     dateService: DateService)
                                     extends uk.gov.dvla.vehicles.presentation.common.controllers.CookiePolicy {

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.cookie_policy(cookies))
  }
}
