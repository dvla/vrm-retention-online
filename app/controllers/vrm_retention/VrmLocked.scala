package controllers.vrm_retention

import com.google.inject.Inject
import common.ClientSideSessionFactory
import common.CookieImplicits.{RichCookies, RichSimpleResult}
import constraints.common.RegistrationNumber.formatVrm
import mappings.vrm_retention.RelatedCacheKeys
import models.domain.common.{BruteForcePreventionViewModel, VehicleDetailsModel}
import models.domain.vrm_retention.{VehicleAndKeeperDetailsModel, VehicleAndKeeperLookupFormModel, VrmLockedViewModel}
import play.api.Logger
import play.api.mvc.{Action, Controller}
import utils.helpers.Config

final class VrmLocked @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                  config: Config) extends Controller {

  def present = Action {
    implicit request =>
      (request.cookies.getModel[BruteForcePreventionViewModel],
        request.cookies.getModel[VehicleAndKeeperLookupFormModel],
        request.cookies.getModel[VehicleAndKeeperDetailsModel]) match {
        case (Some(bruteForcePreventionViewModel), Some(vehicleAndKeeperLookupFormModel), Some(vehicleAndKeeperDetails)) =>
          Logger.debug(s"VrmLocked - Displaying the vrm locked error page")
          Ok(views.html.vrm_retention.vrm_locked(createViewModel(vehicleAndKeeperDetails), bruteForcePreventionViewModel.dateTimeISOChronology))
        case (Some(bruteForcePreventionViewModel), Some(vehicleAndKeeperLookupFormModel), None) =>
          Logger.debug(s"VrmLocked - Displaying the vrm locked error page")
          Ok(views.html.vrm_retention.vrm_locked(createViewModel(vehicleAndKeeperLookupFormModel), bruteForcePreventionViewModel.dateTimeISOChronology))
        case _ =>
          Logger.debug("VrmLocked - Can't find cookies")
          Redirect(routes.VehicleLookup.present())
      }
  }

  def exit = Action {
    implicit request =>
      Redirect(routes.BeforeYouStart.present()).discardingCookies(RelatedCacheKeys.FullSet)
  }

  private def createViewModel(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel): VrmLockedViewModel = // TODO can be moved to an apply function on a companion object.
    VrmLockedViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      vehicleMake = vehicleAndKeeperDetails.vehicleMake,
      vehicleModel = vehicleAndKeeperDetails.vehicleModel)

  private def createViewModel(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel): VrmLockedViewModel = // TODO can be moved to an apply function on a companion object.
    VrmLockedViewModel(
      registrationNumber = formatVrm(vehicleAndKeeperLookupFormModel.registrationNumber),
      vehicleMake = None,
      vehicleModel = None)
}