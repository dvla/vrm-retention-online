package controllers.vrm_retention

import com.google.inject.Inject
import common.ClientSideSessionFactory
import common.CookieImplicits.{RichCookies, RichForm, RichSimpleResult}
import mappings.common.Postcode._
import mappings.vrm_retention.SetupBusinessDetails._
import models.domain.vrm_retention.VehicleAndKeeperDetailsModel
import models.domain.vrm_retention.{SetupBusinessDetailsFormModel, SetupBusinessDetailsViewModel}
import play.api.data.Forms._
import play.api.data.{Form, FormError}
import play.api.mvc._
import utils.helpers.Config
import utils.helpers.FormExtensions._
import play.api.data.validation.Constraints

final class SetUpBusinessDetails @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                             config: Config) extends Controller {

  private[vrm_retention] val form = Form(
    mapping(
      BusinessNameId -> businessName(),
      BusinessContactId -> businessContact(),
      BusinessPostcodeId -> postcode
    )(SetupBusinessDetailsFormModel.apply)(SetupBusinessDetailsFormModel.unapply)
  )

  def present = Action {
    implicit request =>
      request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
        case Some(vehicleAndKeeperDetails) =>
          val setupBusinessDetailsViewModel = SetupBusinessDetailsViewModel(vehicleAndKeeperDetails)
          Ok(views.html.vrm_retention.setup_business_details(form.fill(), setupBusinessDetailsViewModel))
        case _ => Redirect(routes.VehicleLookup.present()) // US320 the user has pressed back button after being on dispose-success and pressing new dispose.
      }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => {
        request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
          case Some(vehicleAndKeeperDetailsModel) =>
            val setupBusinessDetailsViewModel = SetupBusinessDetailsViewModel(vehicleAndKeeperDetailsModel)
            val formWithReplacedErrors = invalidForm.
              replaceError(BusinessNameId, FormError(key = BusinessNameId,
              message = "error.validBusinessName",
              args = Seq.empty)).
              replaceError(BusinessContactId, FormError(key = BusinessContactId,
              message = "error.validBusinessContact",
              args = Seq.empty)).
              replaceError(BusinessPostcodeId, FormError(key = BusinessPostcodeId,
              message = "error.restricted.validPostcode",
              args = Seq.empty)).
              distinctErrors
            BadRequest(views.html.vrm_retention.setup_business_details(formWithReplacedErrors,
              setupBusinessDetailsViewModel))
          case _ =>
            Redirect(routes.VehicleLookup.present())
        }
      },
      validForm => Redirect(routes.BusinessChooseYourAddress.present()).withCookie(validForm)
    )
  }
}