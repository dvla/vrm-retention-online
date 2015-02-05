package controllers

import com.google.inject.Inject
import play.api.mvc.Action
import play.api.mvc.Controller
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getOptionalProperty
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.stringProp

/* Controller for redirecting people to the start page if the enter the application using the url "/"
* This allows us to change the start page using the config file without having to change any code. */
final class Application @Inject()() extends Controller {

  private final val startUrl: String = getOptionalProperty[String]("start.page").getOrElse("/before-you-start")

  def index = Action {
    Redirect(startUrl)
  }
}