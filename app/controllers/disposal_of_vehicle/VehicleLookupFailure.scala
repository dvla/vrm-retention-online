package controllers.disposal_of_vehicle

import com.google.inject.Inject
import common.ClientSideSessionFactory
import common.CookieImplicits.RichCookies
import mappings.disposal_of_vehicle.VehicleLookup.VehicleLookupResponseCodeCacheKey
import models.domain.disposal_of_vehicle.{TraderDetailsModel, VehicleLookupFormModel}
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Controller, DiscardingCookie, Request}
import utils.helpers.Config
import models.domain.common.BruteForcePreventionViewModel

final class VehicleLookupFailure @Inject()()
                                 (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                  config: Config) extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getModel[TraderDetailsModel],
      request.cookies.getModel[BruteForcePreventionViewModel],
      request.cookies.getModel[VehicleLookupFormModel],
      request.cookies.getString(VehicleLookupResponseCodeCacheKey)) match {
      case (Some(dealerDetails),
            Some(bruteForcePreventionResponse),
            Some(vehicleLookUpFormModelDetails),
            Some(vehicleLookupResponseCode)) =>
        displayVehicleLookupFailure(
          vehicleLookUpFormModelDetails,
          bruteForcePreventionResponse,
          vehicleLookupResponseCode
        )
      case _ => Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def submit = Action { implicit request =>
    (request.cookies.getModel[TraderDetailsModel], request.cookies.getModel[VehicleLookupFormModel]) match {
      case (Some(dealerDetails), Some(vehicleLookUpFormModelDetails)) =>
        Logger.debug("Found dealer and vehicle details")
        Redirect(routes.VehicleLookup.present())
      case _ => Redirect(routes.BeforeYouStart.present())
    }
  }

  private def displayVehicleLookupFailure(vehicleLookUpFormModelDetails: VehicleLookupFormModel,
                                          bruteForcePreventionViewModel: BruteForcePreventionViewModel,
                                          vehicleLookupResponseCode: String)(implicit request: Request[AnyContent]) = {
    Ok(views.html.disposal_of_vehicle.vehicle_lookup_failure(
      data = vehicleLookUpFormModelDetails,
      responseCodeVehicleLookupMSErrorMessage = vehicleLookupResponseCode,
      attempts = bruteForcePreventionViewModel.attempts,
      maxAttempts = bruteForcePreventionViewModel.maxAttempts)
    ).
    discardingCookies(DiscardingCookie(name = VehicleLookupResponseCodeCacheKey))
  }
}