package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.filters.DateTimeZoneService
import utils.helpers.Config

final class Closed @Inject()()(implicit config: Config,
                               timeZone: DateTimeZoneService,
                               dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.closed("8.00am", "6.00pm"))
  }
}