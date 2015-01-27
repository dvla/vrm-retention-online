package controllers

import audit1._
import com.google.inject.Inject
import models._
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, Controller, Request}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClearTextClientSideSessionFactory, ClientSideSessionFactory}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.formBinding
import utils.helpers.{Config, Config2}
import views.html.vrm_retention.enter_address_manually
import views.vrm_retention.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_retention.VehicleLookup._
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest

final class EnterAddressManually @Inject()(
                                            auditService1: audit1.AuditService,
                                            auditService2: audit2.AuditService,
                                            dateService: DateService
                                            )
                                          (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                           config: Config,
                                           config2: Config2) extends Controller {

  private[controllers] val form = Form(
    EnterAddressManuallyModel.Form.Mapping
  )

  def present = Action { implicit request =>
    (request.cookies.getModel[SetupBusinessDetailsFormModel], request.cookies.getModel[VehicleAndKeeperDetailsModel]) match {
      case (Some(setupBusinessDetailsForm), Some(vehicleAndKeeperDetails)) =>
        val viewModel = EnterAddressManuallyViewModel(setupBusinessDetailsForm, vehicleAndKeeperDetails)
        Ok(enter_address_manually(viewModel, form.fill()))
      case _ => Redirect(routes.SetUpBusinessDetails.present())
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        (request.cookies.getModel[SetupBusinessDetailsFormModel], request.cookies.getModel[VehicleAndKeeperDetailsModel]) match {
          case (Some(setupBusinessDetailsForm), Some(vehicleAndKeeperDetails)) =>
            val viewModel = EnterAddressManuallyViewModel(setupBusinessDetailsForm, vehicleAndKeeperDetails)
            BadRequest(enter_address_manually(viewModel, formWithReplacedErrors(invalidForm)))
          case _ =>
            Logger.debug("Failed to find either setupBusinessDetailsForm or vehicleAndKeeperDetails in cache on submit, redirecting")
            Redirect(routes.SetUpBusinessDetails.present())
        },
      validForm =>
        (request.cookies.getModel[SetupBusinessDetailsFormModel], request.cookies.getModel[VehicleAndKeeperDetailsModel]) match {
          case (Some(setupBusinessDetailsForm), Some(vehicleAndKeeperDetails)) =>

            val viewModel = BusinessDetailsModel.from(setupBusinessDetailsForm, vehicleAndKeeperDetails, validForm)

            auditService1.send(AuditMessage.from(
              pageMovement = AuditMessage.CaptureActorToConfirmBusiness,
              transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
              timestamp = dateService.dateTimeISOChronology,
              vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
              replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
              businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))
            auditService2.send(AuditRequest.from(
              pageMovement = AuditMessage.CaptureActorToConfirmBusiness,
              transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
              timestamp = dateService.dateTimeISOChronology,
              vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
              replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
              businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))

            Redirect(routes.ConfirmBusiness.present())
              .withCookie(validForm)
              .withCookie(viewModel)
          case _ =>
            Logger.debug("Failed to find either setupBusinessDetailsForm or vehicleAndKeeperDetails in cache on submit, redirecting")
            Redirect(routes.SetUpBusinessDetails.present())
        }
    )
  }

  def exit = Action { implicit request =>
    auditService1.send(AuditMessage.from(
      pageMovement = AuditMessage.CaptureActorToExit,
      transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
      timestamp = dateService.dateTimeISOChronology,
      vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
      replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
      businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))
    auditService2.send(AuditRequest.from(
      pageMovement = AuditMessage.CaptureActorToExit,
      transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
      timestamp = dateService.dateTimeISOChronology,
      vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
      replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
      businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))

    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }

  private def formWithReplacedErrors(form: Form[EnterAddressManuallyModel])(implicit request: Request[_]) =
    form.
      replaceError(
        "addressAndPostcode.addressLines.buildingNameOrNumber",
        FormError(
          key = "addressAndPostcode.addressLines",
          message = "error.address.buildingNameOrNumber.invalid"
        )
      ).
      replaceError(
        "addressAndPostcode.addressLines.postTown",
        FormError(
          key = "addressAndPostcode.addressLines",
          message = "error.address.postTown"
        )
      ).
      replaceError(
        "addressAndPostcode.postcode",
        FormError("addressAndPostcode.postcode", "error.address.postcode.invalid")
      ).distinctErrors
}