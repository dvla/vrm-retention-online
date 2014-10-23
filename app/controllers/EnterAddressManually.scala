package controllers

import com.google.inject.Inject
import models._
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, Controller, Request}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config
import views.html.vrm_retention.enter_address_manually
import audit.{CaptureActorToConfirmBusinessAuditMessage, AuditService, ConfirmBusinessToConfirmAuditMessage}
import views.vrm_retention.VehicleLookup._
import scala.Some
import views.vrm_retention.CheckEligibility._
import scala.Some

final class EnterAddressManually @Inject()(auditService: AuditService)
                                          (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                           config: Config) extends Controller {

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

            val transactionId = request.cookies.getString(TransactionIdCacheKey).get
            val replacementVRM = request.cookies.getString(CheckEligibilityCacheKey).get
            val vehicleAndKeeperLookup = request.cookies.getModel[VehicleAndKeeperLookupFormModel].get

            auditService.send(CaptureActorToConfirmBusinessAuditMessage.from(transactionId,
            vehicleAndKeeperLookup, vehicleAndKeeperDetails, replacementVRM, viewModel))

            Redirect(routes.ConfirmBusiness.present())
              .withCookie(validForm)
              .withCookie(viewModel)
          case _ =>
            Logger.debug("Failed to find either setupBusinessDetailsForm or vehicleAndKeeperDetails in cache on submit, redirecting")
            Redirect(routes.SetUpBusinessDetails.present())
        }
    )
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