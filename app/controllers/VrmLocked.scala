package controllers

import com.google.inject.Inject
import org.joda.time.DateTime
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel
import utils.helpers.Config
import viewmodels.{VehicleAndKeeperDetailsModel, VehicleAndKeeperLookupFormModel, VrmLockedViewModel}
import views.vrm_retention.RelatedCacheKeys
import views.vrm_retention.VehicleLookup._

final class VrmLocked @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                  config: Config) extends Controller {

  def present = Action {
    implicit request =>
      (request.cookies.getString(TransactionIdCacheKey),
        request.cookies.getModel[BruteForcePreventionModel],
        request.cookies.getModel[VehicleAndKeeperLookupFormModel],
        request.cookies.getModel[VehicleAndKeeperDetailsModel]) match {
        case (Some(transactionId), Some(bruteForcePreventionModel), _, Some(vehicleAndKeeperDetails)) =>
          Logger.debug(s"VrmLocked - Displaying the vrm locked error page")
          val timeString: String = bruteForcePreventionModel.dateTimeISOChronology
          val javascriptTimestamp: Long = DateTime.parse(timeString).getMillis
          Ok(views.html.vrm_retention.vrm_locked(transactionId, VrmLockedViewModel(vehicleAndKeeperDetails, timeString, javascriptTimestamp)))
        case (Some(transactionId), Some(bruteForcePreventionModel), Some(vehicleAndKeeperLookupForm), None) =>
          Logger.debug(s"VrmLocked - Displaying the vrm locked error page")
          val timeString: String = bruteForcePreventionModel.dateTimeISOChronology
          val javascriptTimestamp: Long = DateTime.parse(timeString).getMillis
          Ok(views.html.vrm_retention.vrm_locked(transactionId, VrmLockedViewModel(vehicleAndKeeperLookupForm, timeString, javascriptTimestamp)))
        case _ =>
          Logger.debug("VrmLocked - Can't find cookies")
          Redirect(routes.VehicleLookup.present())
      }
  }

  def exit = Action {
    implicit request =>
      Redirect(routes.MockFeedback.present()).
        discardingCookies(RelatedCacheKeys.RetainSet)
    // TODO remove Business Cache if consent not sent
  }
}