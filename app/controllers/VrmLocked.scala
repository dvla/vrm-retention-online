package controllers

import com.google.inject.Inject
import models.{VehicleAndKeeperDetailsModel, VehicleAndKeeperLookupFormModel, VrmLockedViewModel}
import org.joda.time.DateTime
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel
import utils.helpers.Config
import views.vrm_retention.Confirm.StoreBusinessDetailsCacheKey
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
          val viewModel = {
            val timeString: String = bruteForcePreventionModel.dateTimeISOChronology
            val javascriptTimestamp: Long = DateTime.parse(timeString).getMillis
            VrmLockedViewModel(vehicleAndKeeperDetails, timeString, javascriptTimestamp)
          }
          Ok(views.html.vrm_retention.vrm_locked(transactionId, viewModel))
        case (Some(transactionId), Some(bruteForcePreventionModel), Some(vehicleAndKeeperLookupForm), None) =>
          Logger.debug(s"VrmLocked - Displaying the vrm locked error page")
          val viewModel = {
            val timeString: String = bruteForcePreventionModel.dateTimeISOChronology
            val javascriptTimestamp: Long = DateTime.parse(timeString).getMillis
            VrmLockedViewModel(vehicleAndKeeperLookupForm, timeString, javascriptTimestamp)
          }
          Ok(views.html.vrm_retention.vrm_locked(transactionId, viewModel))
        case _ =>
          Logger.debug("VrmLocked - Can't find cookies")
          Redirect(routes.VehicleLookup.present()) // TODO need an error page with a message to explain that there is a cookie problem.
      }
  }

  def exit = Action {
    implicit request =>
      val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)
      val cacheKeys = RelatedCacheKeys.RetainSet ++ {
        if (storeBusinessDetails) Set.empty else RelatedCacheKeys.BusinessDetailsSet
      }
      Redirect(routes.MockFeedback.present()).discardingCookies(cacheKeys)
  }
}