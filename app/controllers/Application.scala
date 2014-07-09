package controllers

import app.ConfigProperties.getProperty
import play.api.mvc.{Action, Controller}

/* Controller for redirecting people to the start page if the enter the application using the url "/"
* This allows us to change the start page using the config file without having to change any code. */
object Application extends Controller {
  private final val startUrl: String = getProperty("start.page", "/sell-to-the-trade/before-you-start")

  def index = Action {
    Redirect(startUrl)
  }
}