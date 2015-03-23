package controllers

import com.google.inject.Inject
import models.CacheKeyPrefix
import models.VehicleAndKeeperLookupFormModel
import models.VehicleLookupFailureViewModel
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.html.vrm_retention.direct_to_paper
import views.html.vrm_retention.postcode_mismatch
import views.html.vrm_retention.vehicle_lookup_failure
import views.vrm_retention.VehicleLookup._

final class VehicleLookupFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                             config: Config,
                                             dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService) extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getString(VehicleAndKeeperLookupResponseCodeCacheKey),
      request.cookies.getModel[VehicleAndKeeperDetailsModel]
      ) match {
      case (Some(transactionId), Some(vehicleAndKeeperLookupForm), Some(vehicleLookupResponseCode), vehicleAndKeeperDetails) =>
        displayVehicleLookupFailure(
          transactionId,
          vehicleAndKeeperLookupForm,
          vehicleAndKeeperDetails,
          vehicleLookupResponseCode
        )
      case _ => Redirect(routes.BeforeYouStart.present())
    }
  }

  def submit = Action { implicit request =>
    request.cookies.getModel[VehicleAndKeeperLookupFormModel] match {
      case (Some(vehicleAndKeeperLookupFormModel)) =>
        Redirect(routes.VehicleLookup.present())
      case _ => Redirect(routes.BeforeYouStart.present())
    }
  }

  def tryAgain = Action { implicit request =>
    Redirect(routes.VehicleLookup.present())
  }

  private def displayVehicleLookupFailure(transactionId: String,
                                          vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
                                          vehicleAndKeeperDetails: Option[VehicleAndKeeperDetailsModel],
                                          vehicleAndKeeperLookupResponseCode: String)(implicit request: Request[AnyContent]) = {
    val viewModel = vehicleAndKeeperDetails match {
      case Some(details) => VehicleLookupFailureViewModel(details)
      case None => VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm)
    }

    vehicleAndKeeperLookupResponseCode match {
      case "vrm_retention_eligibility_direct_to_paper" =>
        Ok(direct_to_paper(
          transactionId = transactionId,
          viewModel = viewModel
        )
        ).
          discardingCookies(DiscardingCookie(name = VehicleAndKeeperLookupResponseCodeCacheKey))
      case "vehicle_and_keeper_lookup_keeper_postcode_mismatch" =>
        Ok(postcode_mismatch(
          transactionId = transactionId,
          viewModel = viewModel
        )
        ).
          discardingCookies(DiscardingCookie(name = VehicleAndKeeperLookupResponseCodeCacheKey))
      case _ =>
        Ok(vehicle_lookup_failure(
          transactionId = transactionId,
          viewModel = viewModel,
          responseCodeVehicleLookupMSErrorMessage = vehicleAndKeeperLookupResponseCode
        )
        ).
          discardingCookies(DiscardingCookie(name = VehicleAndKeeperLookupResponseCodeCacheKey))
    }
  }
}