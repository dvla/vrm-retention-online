package controllers.vrm_retention

import com.google.inject.Inject
import common.ClientSideSessionFactory
import common.CookieImplicits.RichCookies
import mappings.vrm_retention.VehicleLookup._
import models.domain.common.BruteForcePreventionViewModel
import models.domain.vrm_retention.{VehicleAndKeeperDetailsModel, VehicleAndKeeperLookupFormModel, VehicleLookupFailureViewModel}
import play.api.Logger
import play.api.mvc._
import utils.helpers.Config

final class VehicleLookupFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                             config: Config) extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getModel[BruteForcePreventionViewModel],
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getString(VehicleAndKeeperLookupResponseCodeCacheKey)) match {
      case (Some(bruteForcePreventionResponse), Some(vehicleAndKeeperLookupFormModel), Some(vehicleAndKeeperDetails), Some(vehicleLookupResponseCode)) =>
        displayVehicleLookupFailure(vehicleAndKeeperLookupFormModel, bruteForcePreventionResponse, Some(vehicleAndKeeperDetails), vehicleLookupResponseCode)
      case (Some(bruteForcePreventionResponse), Some(vehicleAndKeeperLookupFormModel), None, Some(vehicleLookupResponseCode)) =>
        displayVehicleLookupFailure(vehicleAndKeeperLookupFormModel, bruteForcePreventionResponse, None, vehicleLookupResponseCode)
      case _ => Redirect(routes.BeforeYouStart.present())
    }
  }

  def submit = Action { implicit request =>
    request.cookies.getModel[VehicleAndKeeperLookupFormModel] match {
      case (Some(vehicleAndKeeperLookupFormModel)) =>
        Logger.debug("Found vehicle details")
        Redirect(routes.VehicleLookup.present())
      case _ => Redirect(routes.BeforeYouStart.present())
    }
  }

  private def displayVehicleLookupFailure(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                                          bruteForcePreventionViewModel: BruteForcePreventionViewModel,
                                          vehicleAndKeeperDetails: Option[VehicleAndKeeperDetailsModel],
                                          vehicleAndKeeperLookupResponseCode: String)(implicit request: Request[AnyContent]) = {
    val vehicleLookupFailureViewModel = vehicleAndKeeperDetails match {
      case Some(details) => VehicleLookupFailureViewModel(details)
      case None => VehicleLookupFailureViewModel(vehicleAndKeeperLookupFormModel)
    }

    Ok(views.html.vrm_retention.vehicle_lookup_failure(
      vehicleLookupFailureViewModel = vehicleLookupFailureViewModel,
      data = vehicleAndKeeperLookupFormModel,
      responseCodeVehicleLookupMSErrorMessage = vehicleAndKeeperLookupResponseCode,
      attempts = bruteForcePreventionViewModel.attempts,
      maxAttempts = bruteForcePreventionViewModel.maxAttempts)
    ).
      discardingCookies(DiscardingCookie(name = VehicleAndKeeperLookupResponseCodeCacheKey))
  }
}