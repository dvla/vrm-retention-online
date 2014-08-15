package controllers

import com.google.inject.Inject
import views.vrm_retention.RelatedCacheKeys
import viewmodels.{VehicleAndKeeperLookupFormModel, VrmLockedViewModel}
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel
import play.api.Logger
import viewmodels.VehicleAndKeeperDetailsModel
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichSimpleResult}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

final class VrmLocked @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                  config: Config) extends Controller {

  def present = Action {
    implicit request =>
      (request.cookies.getModel[BruteForcePreventionModel],
        request.cookies.getModel[VehicleAndKeeperLookupFormModel],
        request.cookies.getModel[VehicleAndKeeperDetailsModel]) match {
        case (Some(bruteForcePreventionViewModel), Some(vehicleAndKeeperLookupFormModel), Some(vehicleAndKeeperDetails)) =>
          Logger.debug(s"VrmLocked - Displaying the vrm locked error page")
          Ok(views.html.vrm_retention.vrm_locked(VrmLockedViewModel(vehicleAndKeeperDetails), bruteForcePreventionViewModel.dateTimeISOChronology))
        case (Some(bruteForcePreventionViewModel), Some(vehicleAndKeeperLookupFormModel), None) =>
          Logger.debug(s"VrmLocked - Displaying the vrm locked error page")
          Ok(views.html.vrm_retention.vrm_locked(VrmLockedViewModel(vehicleAndKeeperLookupFormModel), bruteForcePreventionViewModel.dateTimeISOChronology))
        case _ =>
          Logger.debug("VrmLocked - Can't find cookies")
          Redirect(routes.VehicleLookup.present())
      }
  }

  def exit = Action {
    implicit request =>
      Redirect(routes.MockFeedback.present()).discardingCookies(RelatedCacheKeys.FullSet)
  }
}