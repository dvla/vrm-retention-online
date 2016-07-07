package controllers

import java.util.Locale

import com.google.inject.Inject
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.DateTimeFormat
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

final class TermsAndConditions @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                     config: Config,
                                     dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService)
                                     extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.terms_and_conditions(h(config.openingTimeMinOfDay * MillisInMinute),
      h(config.closingTimeMinOfDay * MillisInMinute)
    ))
  }

  private final val MillisInMinute = 60 * 1000L

  private def h(hourMillis: Long) =
    DateTimeFormat.forPattern("HH:mm").withLocale(Locale.UK)
      .print(new DateTime(hourMillis, DateTimeZone.forID("UTC"))).toLowerCase // Must use UTC as we only want to format the hour
}
