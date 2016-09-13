package controllers

import com.google.inject.Inject
import mappings.common.ErrorCodes
import models.{CacheKeyPrefix, VehicleAndKeeperLookupFormModel, VehicleLookupFailureViewModel}
import play.api.mvc.{Action, AnyContent, Controller, DiscardingCookie, Request}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupBase
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.model.MicroserviceResponseModel
import uk.gov.dvla.vehicles.presentation.common.model.MicroserviceResponseModel.MsResponseCacheKey
import utils.helpers.Config
import views.html.vrm_retention.lookup_failure.direct_to_paper
import views.html.vrm_retention.lookup_failure.eligibility_failure
import views.html.vrm_retention.lookup_failure.ninety_day_rule_failure
import views.html.vrm_retention.lookup_failure.postcode_mismatch
import views.html.vrm_retention.lookup_failure.vehicle_lookup_failure
import views.vrm_retention.VehicleLookup.TransactionIdCacheKey

final class VehicleLookupFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                             config: Config,
                                             dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService
                                            ) extends Controller with DVLALogger {

  def present = Action { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getModel[MicroserviceResponseModel]
      ) match {
      case (Some(transactionId),
        Some(vehicleAndKeeperLookupForm),
        vehicleAndKeeperDetails,
        Some(failureModel)) =>

        displayVehicleLookupFailure(
          transactionId,
          vehicleAndKeeperLookupForm,
          vehicleAndKeeperDetails,
          failureModel
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

  private def directToPaper(viewModel: VehicleLookupFailureViewModel,
                            transactionId: String,
                            maybeResponseMessage: Option[String] = None,
                            maybeResponseLink: Option[String] = None)
                           (implicit request: Request[AnyContent]) = {
    logMessage(request.cookies.trackingId(), Info, s"VehicleLookupFailure is presenting direct to paper view")
    direct_to_paper(
      transactionId = transactionId,
      viewModel = viewModel,
      responseMessage = maybeResponseMessage,
      responseLink = maybeResponseLink
    )
  }

  private def vehicleLookupFailure(viewModel: VehicleLookupFailureViewModel,
                                   msResponseModel: MicroserviceResponseModel,
                                   transactionId: String)
                                  (implicit request: Request[AnyContent]) = {
    logMessage(request.cookies.trackingId(), Info, "VehicleLookupFailure is presenting vehicle lookup failure view")
    vehicle_lookup_failure(
      transactionId = transactionId,
      viewModel = viewModel,
      responseCodeVehicleLookupMSErrorMessage = msResponseModel.msResponse.message
    )
  }

  private def postcodeMismatch(viewModel: VehicleLookupFailureViewModel,
                               transactionId: String)
                              (implicit request: Request[AnyContent]) = {
    logMessage(request.cookies.trackingId(), Info, "VehicleLookupFailure is presenting postcode mismatch view")
    postcode_mismatch(
      transactionId = transactionId,
      viewModel = viewModel.copy(failureCode = ErrorCodes.PostcodeMismatchErrorCode)
    )
  }

  private def eligibilityFailure(viewModel: VehicleLookupFailureViewModel,
                                 transactionId: String,
                                 maybeResponseMessage: Option[String] = None,
                                 maybeResponseLink: Option[String] = None)
                                (implicit request: Request[AnyContent]) = {
    logMessage(request.cookies.trackingId(), Info, "VehicleLookupFailure is presenting eligibility failure view")
    eligibility_failure(
      transactionId = transactionId,
      viewModel = viewModel,
      responseMessage = maybeResponseMessage,
      responseLink = maybeResponseLink
    )
  }

  private def ninetyDayRuleFailure(viewModel: VehicleLookupFailureViewModel,
                                   transactionId: String)
                                  (implicit request: Request[AnyContent]) = {
    logMessage(request.cookies.trackingId(), Info, s"VehicleLookupFailure is presenting ninety day rule failure view")
    ninety_day_rule_failure(
      transactionId = transactionId,
      viewModel = viewModel
    )
  }

  private def displayVehicleLookupFailure(transactionId: String,
                                          vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
                                          vehicleAndKeeperDetails: Option[VehicleAndKeeperDetailsModel],
                                          msResponseModel: MicroserviceResponseModel)
                                         (implicit request: Request[AnyContent]) = {
    val viewModel = VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm, vehicleAndKeeperDetails, msResponseModel.msResponse.code)

    val failurePage = msResponseModel.msResponse.message match {
      case "vrm_retention_eligibility_direct_to_paper" => directToPaper(viewModel, transactionId)
      case VehicleLookupBase.RESPONSE_CODE_POSTCODE_MISMATCH => postcodeMismatch(viewModel, transactionId)
      case "vrm_retention_eligibility_failure" => eligibilityFailure(viewModel, transactionId)
      case "vrm_retention_eligibility_ninety_day_rule_failure" => ninetyDayRuleFailure(viewModel, transactionId)
      case "vrm_retention_eligibility_exported_failure" =>
        eligibilityFailure(viewModel, transactionId, Some("vrm_retention_eligibility_exported_failure"))
      case "vrm_retention_eligibility_scrapped_failure" =>
        eligibilityFailure(viewModel, transactionId, Some("vrm_retention_eligibility_scrapped_failure"))
      case "vrm_retention_eligibility_damaged_failure" =>
        directToPaper(viewModel, transactionId, Some("vrm_retention_eligibility_damaged_failure"))
      case "vrm_retention_eligibility_vic_failure" =>
        directToPaper(viewModel, transactionId, Some("vrm_retention_eligibility_vic_failure"),
          Some("vrm_retention_eligibility_vic_failure_link"))
      case "vrm_retention_eligibility_no_keeper_failure" =>
        eligibilityFailure(viewModel, transactionId, Some("vrm_retention_eligibility_no_keeper_failure"),
          Some("vrm_retention_eligibility_no_keeper_failure_link"))
      case "vrm_retention_eligibility_not_mot_failure" =>
        eligibilityFailure(viewModel, transactionId, Some("vrm_retention_eligibility_not_mot_failure"))
      case "vrm_retention_eligibility_pre_1998_failure" =>
        directToPaper(viewModel, transactionId, Some("vrm_retention_eligibility_pre_1998_failure"))
      case "vrm_retention_eligibility_q_plate_failure" =>
        eligibilityFailure(viewModel, transactionId, Some("vrm_retention_eligibility_q_plate_failure"))
      case _ => vehicleLookupFailure(viewModel, msResponseModel, transactionId)
    }

    Ok(failurePage).discardingCookies(DiscardingCookie(name = MsResponseCacheKey))
  }
}
