package controllers

import com.google.inject.Inject
import play.api.data.{Form, FormError}
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import utils.helpers.Config
import viewmodels.{SetupBusinessDetailsFormModel, SetupBusinessDetailsViewModel, VehicleAndKeeperDetailsModel}
import views.vrm_retention.SetupBusinessDetails
import views.vrm_retention.SetupBusinessDetails._

final class SetUpBusinessDetails @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                             config: Config) extends Controller {

  private[controllers] val form = Form(
    SetupBusinessDetailsFormModel.Form.Mapping
  )

  def present = Action {
    implicit request =>
      request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
        case Some(vehicleAndKeeperDetails) =>
          val viewModel = SetupBusinessDetailsViewModel(vehicleAndKeeperDetails)
          Ok(views.html.vrm_retention.setup_business_details(form.fill(), viewModel))
        case _ => Redirect(routes.VehicleLookup.present())
      }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => {
        request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
          case Some(vehicleAndKeeperDetails) =>
            val setupBusinessDetailsViewModel = SetupBusinessDetailsViewModel(vehicleAndKeeperDetails)
            BadRequest(views.html.vrm_retention.setup_business_details(formWithReplacedErrors(invalidForm),
              setupBusinessDetailsViewModel))
          case _ =>
            Redirect(routes.VehicleLookup.present())
        }
      },
      validForm => Redirect(routes.BusinessChooseYourAddress.present()).withCookie(validForm)
    )
  }

  private def formWithReplacedErrors(form: Form[SetupBusinessDetailsFormModel])(implicit request: Request[_]) =
    form.
      replaceError(BusinessNameId,
        FormError(
          key = BusinessNameId,
          message = "error.validBusinessName",
          args = Seq.empty)).
      replaceError(BusinessContactId,
        FormError(
          key = BusinessContactId,
          message = "error.validBusinessContact",
          args = Seq.empty)).
      replaceError(BusinessEmailId,
        FormError(
          key = BusinessEmailId,
          message = "error.validEmail",
          args = Seq.empty)).
      replaceError(BusinessPostcodeId,
        FormError(
          key = BusinessPostcodeId,
          message = "error.restricted.validPostcode",
          args = Seq.empty)).
      distinctErrors
}