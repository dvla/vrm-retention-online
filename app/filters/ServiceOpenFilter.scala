package filters

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.filters.{EnsureServiceOpenFilter, DateTimeZoneService}
import utils.helpers.Config

class ServiceOpenFilter @Inject()(implicit config: Config,
                                  timeZone: DateTimeZoneService,
                                  dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService) extends EnsureServiceOpenFilter {
  protected lazy val opening = config.opening
  protected lazy val closing = config.closing
  protected lazy val dateTimeZone = timeZone
  protected lazy val html = views.html.vrm_retention.closed("", "")

  override protected def html(opening: String, closing: String) = views.html.vrm_retention.closed(opening, closing)
}