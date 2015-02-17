package controllers

import com.google.inject.Inject
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.filters.DateTimeZoneService
import utils.helpers.Config

final class Closed @Inject()()(implicit config: Config,
                               timeZone: DateTimeZoneService) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.closed("8.00am", "6.00pm"))
  }
}