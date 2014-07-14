package controllers.vrm_retention

import play.api.Logger
import play.api.mvc._
import com.google.inject.Inject
import models.domain.vrm_retention.{VehicleLookupFailureViewModel, VehicleLookupFormModel}
import mappings.vrm_retention.VehicleLookup._
import common.{ClientSideSessionFactory, CookieImplicits}
import CookieImplicits.RichSimpleResult
import CookieImplicits.RichCookies
import CookieImplicits.RichForm
import play.api.mvc.DiscardingCookie
import play.api.Play.current
import models.domain.common.{VehicleDetailsModel, BruteForcePreventionViewModel}
import utils.helpers.Config

final class VehicleLookupFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory, config: Config) extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getModel[BruteForcePreventionViewModel],
      request.cookies.getModel[VehicleLookupFormModel],
      request.cookies.getModel[VehicleDetailsModel],
      request.cookies.getString(VehicleLookupResponseCodeCacheKey)) match {
      case (Some(bruteForcePreventionResponse), Some(vehicleLookUpFormModelDetails), Some(vehicleDetails), Some(vehicleLookupResponseCode)) =>
        displayVehicleLookupFailure(vehicleLookUpFormModelDetails, bruteForcePreventionResponse, Some(vehicleDetails), vehicleLookupResponseCode)
      case (Some(bruteForcePreventionResponse), Some(vehicleLookUpFormModelDetails), None, Some(vehicleLookupResponseCode)) =>
        displayVehicleLookupFailure(vehicleLookUpFormModelDetails, bruteForcePreventionResponse, None, vehicleLookupResponseCode)
      case _ => Redirect(routes.BeforeYouStart.present())
    }
  }

  def submit = Action { implicit request =>
    (request.cookies.getModel[VehicleLookupFormModel]) match {
      case (Some(vehicleLookUpFormModelDetails)) =>
        Logger.debug("Found vehicle details")
        Redirect(routes.VehicleLookup.present())
      case _ => Redirect(routes.BeforeYouStart.present())
    }
  }

  private def displayVehicleLookupFailure(vehicleLookUpFormModelDetails: VehicleLookupFormModel,
                                          bruteForcePreventionViewModel: BruteForcePreventionViewModel,
                                          vehicleDetails: Option[VehicleDetailsModel],
                                          vehicleLookupResponseCode: String)(implicit request: Request[AnyContent]) = {

    Ok(views.html.vrm_retention.vehicle_lookup_failure(
      if (vehicleDetails.isDefined) createViewModel(vehicleDetails.get) else createViewModel(vehicleLookUpFormModelDetails),
      data = vehicleLookUpFormModelDetails,
      responseCodeVehicleLookupMSErrorMessage = vehicleLookupResponseCode,
      attempts = bruteForcePreventionViewModel.attempts,
      maxAttempts = bruteForcePreventionViewModel.maxAttempts)
    ).
      discardingCookies(DiscardingCookie(name = VehicleLookupResponseCodeCacheKey))
  }

  private def createViewModel(vehicleDetails: VehicleDetailsModel): VehicleLookupFailureViewModel =
    VehicleLookupFailureViewModel(
      registrationNumber = vehicleDetails.registrationNumber,
      vehicleMake = Some(vehicleDetails.vehicleMake),
      vehicleModel = Some(vehicleDetails.vehicleModel))

  private def createViewModel(vehicleLookUpFormModelDetails: VehicleLookupFormModel): VehicleLookupFailureViewModel =
    VehicleLookupFailureViewModel(
      registrationNumber = vehicleLookUpFormModelDetails.registrationNumber,
      vehicleMake = None,
      vehicleModel = None)
}
