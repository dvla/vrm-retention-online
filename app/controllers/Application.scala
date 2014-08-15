package controllers

import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getProperty

/* Controller for redirecting people to the start page if the enter the application using the url "/"
* This allows us to change the start page using the config file without having to change any code. */
object Application extends Controller {

  private final val startUrl: String = getProperty("start.page", "/vrm-retention/before-you-start")

  def index = Action {
    Redirect(startUrl)
  }
}