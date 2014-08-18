package controllers

import com.google.inject.Inject
import play.api.data.{Form, FormError}
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import utils.helpers.Config
import viewmodels._
import views.vrm_retention.Confirm.KeeperEmailId
import views.vrm_retention.RelatedCacheKeys

final class Confirm @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {

  private[controllers] val form = Form(ConfirmFormModel.Form.Mapping)

  def present = Action {
    implicit request =>
      (request.cookies.getModel[VehicleAndKeeperDetailsModel], request.cookies.getModel[BusinessDetailsModel]) match {
        case (Some(vehicleAndKeeperDetails), Some(businessDetailsModel)) =>
          val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails, businessDetailsModel)
          Ok(views.html.vrm_retention.confirm(confirmViewModel, form))
        case (Some(vehicleAndKeeperDetails), None) =>
          val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails)
          Ok(views.html.vrm_retention.confirm(confirmViewModel, form))
        case _ =>
          Redirect(routes.VehicleLookup.present())
      }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => {
        (request.cookies.getModel[VehicleAndKeeperDetailsModel], request.cookies.getModel[BusinessDetailsModel]) match {
          case (Some(vehicleAndKeeperDetails), Some(businessDetailsModel)) =>
            val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails, businessDetailsModel)
            val formWithReplacedErrors = invalidForm.
              replaceError(KeeperEmailId,
                FormError(
                  key = KeeperEmailId,
                  message = "error.validEmail",
                  args = Seq.empty)).
              distinctErrors
            BadRequest(views.html.vrm_retention.confirm(confirmViewModel, formWithReplacedErrors))
          case (Some(vehicleAndKeeperDetails), None) =>
            val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails)
            val formWithReplacedErrors = invalidForm.
              replaceError(KeeperEmailId,
                FormError(
                  key = KeeperEmailId,
                  message = "error.validEmail",
                  args = Seq.empty)).
              distinctErrors
            BadRequest(views.html.vrm_retention.confirm(confirmViewModel, formWithReplacedErrors))
          case _ =>
            Redirect(routes.VehicleLookup.present())
        }
      },
      validForm => Redirect(routes.Payment.present()).withCookie(validForm)
    )
  }

  def exit = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present())
      .discardingCookies(RelatedCacheKeys.FullSet)
  }
}