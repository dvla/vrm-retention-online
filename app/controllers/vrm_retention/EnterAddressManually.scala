package controllers.vrm_retention

import com.google.inject.Inject
import common.ClientSideSessionFactory
import common.CookieImplicits.{RichCookies, RichForm, RichSimpleResult}
import mappings.common.AddressAndPostcode.{AddressAndPostcodeId, addressAndPostcode}
import models.domain.vrm_retention.{BusinessDetailsModel, SetupBusinessDetailsFormModel, EnterAddressManuallyViewModel, EnterAddressManuallyModel}
import play.api.Logger
import play.api.data.Forms.mapping
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, Controller, Request}
import utils.helpers.Config
import utils.helpers.FormExtensions.formBinding
import views.html.vrm_retention.enter_address_manually
import models.domain.common.{VehicleDetailsModel, AddressViewModel}

final class EnterAddressManually @Inject()()
                                 (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                  config: Config) extends Controller {

  private[vrm_retention] val form = Form(
    mapping(
      AddressAndPostcodeId -> addressAndPostcode
    )(EnterAddressManuallyModel.apply)(EnterAddressManuallyModel.unapply)
  )

  def present = Action { implicit request =>
    (request.cookies.getModel[SetupBusinessDetailsFormModel], request.cookies.getModel[VehicleDetailsModel]) match {
      case (Some(setupBusinessDetailsFormModel), Some(vehicleDetailsModel)) =>
        val enterAddressManuallyViewModel = createViewModel(setupBusinessDetailsFormModel, vehicleDetailsModel)
        Ok(enter_address_manually(enterAddressManuallyViewModel, form.fill()))
      case _ => Redirect(routes.SetUpBusinessDetails.present())
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        (request.cookies.getModel[SetupBusinessDetailsFormModel], request.cookies.getModel[VehicleDetailsModel]) match {
          case (Some(setupBusinessDetailsFormModel), Some(vehicleDetailsModel)) =>
            val enterAddressManuallyViewModel = createViewModel(setupBusinessDetailsFormModel, vehicleDetailsModel)
            BadRequest(enter_address_manually(enterAddressManuallyViewModel, formWithReplacedErrors(invalidForm)))
          case _ =>
            Logger.debug("Failed to find dealer name in cache, redirecting")
            Redirect(routes.SetUpBusinessDetails.present())
        },
      validForm =>
        (request.cookies.getModel[SetupBusinessDetailsFormModel], request.cookies.getModel[VehicleDetailsModel]) match {
          case (Some(setupBusinessDetailsFormModel), Some(vehicleDetailsModel)) =>
            val enterAddressManuallyViewModel = createViewModel(setupBusinessDetailsFormModel, vehicleDetailsModel)
            val businessAddress = AddressViewModel.from(validForm.addressAndPostcodeModel, enterAddressManuallyViewModel.businessPostCode)
            val businessDetailsModel = BusinessDetailsModel(
              businessName = setupBusinessDetailsFormModel.businessName,
              businessAddress = businessAddress)
            Redirect(routes.Confirm.present()).
              withCookie(validForm).
              withCookie(businessDetailsModel)
          case _ =>
            Logger.debug("Failed to find dealer name in cache on submit, redirecting")
            Redirect(routes.SetUpBusinessDetails.present())
        }
    )
  }

  private def formWithReplacedErrors(form: Form[EnterAddressManuallyModel])(implicit request: Request[_]) =
    form.replaceError(
      "addressAndPostcode.addressLines.buildingNameOrNumber",
      FormError("addressAndPostcode.addressLines", "error.address.buildingNameOrNumber.invalid")
    ).replaceError(
      "addressAndPostcode.addressLines.postTown",
      FormError("addressAndPostcode.addressLines",
      "error.address.postTown")
    ).replaceError(
      "addressAndPostcode.postcode",
      FormError("addressAndPostcode.postcode", "error.address.postcode.invalid")
    ).distinctErrors

  private def createViewModel(setupBusinessDetailsFormModel: SetupBusinessDetailsFormModel,
                              vehicleDetails: VehicleDetailsModel): EnterAddressManuallyViewModel =
    EnterAddressManuallyViewModel(
      registrationNumber = vehicleDetails.registrationNumber,
      vehicleMake = vehicleDetails.vehicleMake,
      vehicleModel = vehicleDetails.vehicleModel,
      businessName = setupBusinessDetailsFormModel.businessName,
      businessPostCode = setupBusinessDetailsFormModel.businessPostcode
    )
}