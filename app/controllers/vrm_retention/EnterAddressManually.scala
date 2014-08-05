package controllers.vrm_retention

import com.google.inject.Inject
import common.ClientSideSessionFactory
import common.CookieImplicits.{RichCookies, RichForm, RichSimpleResult}
import constraints.common.Postcode.formatPostcode
import constraints.common.RegistrationNumber.formatVrm
import mappings.common.AddressAndPostcode.{AddressAndPostcodeId, addressAndPostcode}
import models.domain.common.AddressViewModel
import models.domain.vrm_retention._
import play.api.Logger
import play.api.data.Forms.mapping
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, Controller, Request}
import utils.helpers.Config
import utils.helpers.FormExtensions.formBinding
import views.html.vrm_retention.enter_address_manually

final class EnterAddressManually @Inject()()
                                          (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                           config: Config) extends Controller {

  private[vrm_retention] val form = Form(
    mapping(
      AddressAndPostcodeId -> addressAndPostcode
    )(EnterAddressManuallyModel.apply)(EnterAddressManuallyModel.unapply)
  )

  def present = Action { implicit request =>
    (request.cookies.getModel[SetupBusinessDetailsFormModel], request.cookies.getModel[VehicleAndKeeperDetailsModel]) match {
      case (Some(setupBusinessDetailsFormModel), Some(vehicleAndKeeperDetailsModel)) =>
        val enterAddressManuallyViewModel = EnterAddressManuallyViewModel(setupBusinessDetailsFormModel, vehicleAndKeeperDetailsModel)
        Ok(enter_address_manually(enterAddressManuallyViewModel, form.fill()))
      case _ => Redirect(routes.SetUpBusinessDetails.present())
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        (request.cookies.getModel[SetupBusinessDetailsFormModel], request.cookies.getModel[VehicleAndKeeperDetailsModel]) match {
          case (Some(setupBusinessDetailsFormModel), Some(vehicleAndKeeperDetailsModel)) =>
            val enterAddressManuallyViewModel = EnterAddressManuallyViewModel(setupBusinessDetailsFormModel, vehicleAndKeeperDetailsModel)
            BadRequest(enter_address_manually(enterAddressManuallyViewModel, formWithReplacedErrors(invalidForm)))
          case _ =>
            Logger.debug("Failed to find dealer name in cache, redirecting")
            Redirect(routes.SetUpBusinessDetails.present())
        },
      validForm =>
        (request.cookies.getModel[SetupBusinessDetailsFormModel], request.cookies.getModel[VehicleAndKeeperDetailsModel]) match {
          case (Some(setupBusinessDetailsFormModel), Some(vehicleAndKeeperDetailsModel)) =>
            val businessDetailsModel = BusinessDetailsModel.create(setupBusinessDetailsFormModel, vehicleAndKeeperDetailsModel, validForm)
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
}