package controllers.vrm_retention

import com.google.inject.Inject
import common.ClientSideSessionFactory
import common.CookieImplicits.{RichCookies, RichSimpleResult}
import mappings.vrm_retention.RelatedCacheKeys
import models.domain.common.{BruteForcePreventionViewModel, VehicleDetailsModel}
import models.domain.vrm_retention.{VehicleLookupFormModel, VrmLockedViewModel}
import play.api.Logger
import play.api.mvc.{Action, Controller}
import utils.helpers.Config

final class VrmLocked @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                  config: Config) extends Controller {

  def present = Action {
    implicit request =>
      (request.cookies.getModel[BruteForcePreventionViewModel],
        request.cookies.getModel[VehicleLookupFormModel],
        request.cookies.getModel[VehicleDetailsModel]) match {
        case (Some(bruteForcePreventionViewModel), Some(vehicleLookUpFormModelDetails), Some(vehicleDetails)) =>
          Logger.debug(s"VrmLocked - Displaying the vrm locked error page")
          Ok(views.html.vrm_retention.vrm_locked(createViewModel(vehicleDetails), bruteForcePreventionViewModel.dateTimeISOChronology))
        case (Some(bruteForcePreventionViewModel), Some(vehicleLookUpFormModelDetails), None) =>
          Logger.debug(s"VrmLocked - Displaying the vrm locked error page")
          Ok(views.html.vrm_retention.vrm_locked(createViewModel(vehicleLookUpFormModelDetails), bruteForcePreventionViewModel.dateTimeISOChronology))
        case _ =>
          Logger.debug("VrmLocked - Can't find cookies")
          Redirect(routes.VehicleLookup.present())
      }
  }

  def exit = Action {
    implicit request =>
      Redirect(routes.BeforeYouStart.present()).discardingCookies(RelatedCacheKeys.FullSet)
  }

  private def createViewModel(vehicleDetails: VehicleDetailsModel): VrmLockedViewModel = // TODO can be moved to an apply function on a companion object.
    VrmLockedViewModel(
      registrationNumber = vehicleDetails.registrationNumber,
      vehicleMake = Some(vehicleDetails.vehicleMake),
      vehicleModel = Some(vehicleDetails.vehicleModel))

  private def createViewModel(vehicleLookUpFormModelDetails: VehicleLookupFormModel): VrmLockedViewModel = // TODO can be moved to an apply function on a companion object.
    VrmLockedViewModel(
      registrationNumber = vehicleLookUpFormModelDetails.registrationNumber,
      vehicleMake = None,
      vehicleModel = None)
}