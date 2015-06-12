package controllers

import com.google.inject.Inject
import models.CacheKeyPrefix
import models.VehicleAndKeeperLookupFormModel
import models.VehicleLookupFailureViewModel
import play.api.mvc.{Action, AnyContent, Controller, DiscardingCookie, Request}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.html.vrm_retention.lookup_failure.direct_to_paper
import views.html.vrm_retention.lookup_failure.eligibility_failure
import views.html.vrm_retention.lookup_failure.ninety_day_rule_failure
import views.html.vrm_retention.lookup_failure.postcode_mismatch
import views.html.vrm_retention.lookup_failure.vehicle_lookup_failure
import views.vrm_retention.VehicleLookup.{TransactionIdCacheKey, VehicleAndKeeperLookupResponseCodeCacheKey}

final class VehicleLookupFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                             config: Config,
                                             dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService) extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getString(VehicleAndKeeperLookupResponseCodeCacheKey),
      request.cookies.getModel[VehicleAndKeeperDetailsModel]
      ) match {
      case (Some(transactionId),
        Some(vehicleAndKeeperLookupForm),
        Some(vehicleLookupResponseCode),
        vehicleAndKeeperDetails) =>

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
                                          vehicleAndKeeperLookupResponseCode: String)
                                         (implicit request: Request[AnyContent]) = {
    val viewModel = vehicleAndKeeperDetails match {
      case Some(details) => VehicleLookupFailureViewModel(details)
      case None => VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm)
    }

    val failurePage = vehicleAndKeeperLookupResponseCode match {
      case "vrm_retention_eligibility_direct_to_paper" =>
        direct_to_paper(
          transactionId = transactionId,
          viewModel = viewModel
        )
      case "vehicle_and_keeper_lookup_keeper_postcode_mismatch" =>
        postcode_mismatch(
          transactionId = transactionId,
          viewModel = viewModel
        )
      case "vrm_retention_eligibility_failure" =>
        eligibility_failure(
          transactionId = transactionId,
          viewModel = viewModel
        )
      case "vrm_retention_eligibility_ninety_day_rule_failure" =>
        ninety_day_rule_failure(
            transactionId = transactionId,
            viewModel = viewModel
          )
      case "vrm_retention_eligibility_exported_failure" =>
        eligibility_failure(
          transactionId = transactionId,
          viewModel = viewModel,
          responseMessage = Some("vrm_retention_eligibility_exported_failure")
        )
      case "vrm_retention_eligibility_scrapped_failure" =>
        eligibility_failure(
          transactionId = transactionId,
          viewModel = viewModel,
          responseMessage = Some("vrm_retention_eligibility_scrapped_failure")
        )
      case "vrm_retention_eligibility_damaged_failure" =>
        direct_to_paper(
          transactionId = transactionId,
          viewModel = viewModel,
          responseMessage = Some("vrm_retention_eligibility_damaged_failure")
        )
      case "vrm_retention_eligibility_vic_failure" =>
        direct_to_paper(
          transactionId = transactionId,
          viewModel = viewModel,
          responseMessage = Some("vrm_retention_eligibility_vic_failure"),
          responseLink = Some("vrm_retention_eligibility_vic_failure_link")
        )
      case "vrm_retention_eligibility_no_keeper_failure" =>
        eligibility_failure(
          transactionId = transactionId,
          viewModel = viewModel,
          responseMessage = Some("vrm_retention_eligibility_no_keeper_failure"),
          responseLink = Some("vrm_retention_eligibility_no_keeper_failure_link")
        )
      case "vrm_retention_eligibility_not_mot_failure" =>
        eligibility_failure(
          transactionId = transactionId,
          viewModel = viewModel,
          responseMessage = Some("vrm_retention_eligibility_not_mot_failure")
        )
      case "vrm_retention_eligibility_pre_1998_failure" =>
        direct_to_paper(
          transactionId = transactionId,
          viewModel = viewModel,
          responseMessage = Some("vrm_retention_eligibility_pre_1998_failure")
        )
      case "vrm_retention_eligibility_q_plate_failure" =>
        eligibility_failure(
          transactionId = transactionId,
          viewModel = viewModel,
          responseMessage = Some("vrm_retention_eligibility_q_plate_failure")
        )
      case _ =>
        vehicle_lookup_failure(
          transactionId = transactionId,
          viewModel = viewModel,
          responseCodeVehicleLookupMSErrorMessage = vehicleAndKeeperLookupResponseCode
        )
    }

    Ok(failurePage).discardingCookies(DiscardingCookie(name = VehicleAndKeeperLookupResponseCodeCacheKey))
  }
}