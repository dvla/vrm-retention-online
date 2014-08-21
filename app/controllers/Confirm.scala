package controllers

import com.google.inject.Inject
import play.api.data.{Form, FormError}
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import utils.helpers.Config
import viewmodels._
import views.vrm_retention.Confirm._
import views.vrm_retention.RelatedCacheKeys
import scala.Some


final class Confirm @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {

  private[controllers] val form = Form(ConfirmFormModel.Form.Mapping)

  def present = Action {
    implicit request =>
      (request.cookies.getModel[VehicleAndKeeperDetailsModel], request.cookies.getModel[BusinessDetailsModel],
        request.cookies.getString(StoreBusinessDetailsConsentCacheKey)) match {

        case (Some(vehicleAndKeeperDetails), Some(businessDetailsModel), Some(storeBusinessDetailsConsent)) =>
          val confirmFormModel = ConfirmFormModel(None, storeBusinessDetailsConsent)
          val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails, businessDetailsModel)
          Ok(views.html.vrm_retention.confirm(confirmViewModel, form.fill(confirmFormModel)))

        case (Some(vehicleAndKeeperDetails), Some(businessDetailsModel), None) =>
          val confirmFormModel = ConfirmFormModel(None, "")
          val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails, businessDetailsModel)
          Ok(views.html.vrm_retention.confirm(confirmViewModel, form.fill(confirmFormModel)))

        case (Some(vehicleAndKeeperDetails), None, Some(storeBusinessDetailsConsent)) =>
          val confirmFormModel = ConfirmFormModel(None, storeBusinessDetailsConsent)
          val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails)
          Ok(views.html.vrm_retention.confirm(confirmViewModel, form.fill(confirmFormModel)))

        case (Some(vehicleAndKeeperDetails), None, None) =>
          val confirmFormModel = ConfirmFormModel(None, "")
          val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails)
          Ok(views.html.vrm_retention.confirm(confirmViewModel, form.fill(confirmFormModel)))

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
      validForm => {
        if (validForm.keeperEmail.isDefined) {
          Redirect(routes.Payment.present()).
            withCookie(KeeperEmailCacheKey, validForm.keeperEmail.get).
            withCookie(StoreBusinessDetailsConsentCacheKey, validForm.storeBusinessDetailsConsent)
        } else {
          Redirect(routes.Payment.present()).
            withCookie(StoreBusinessDetailsConsentCacheKey, validForm.storeBusinessDetailsConsent)
        }
      }
    )
  }

  def exit = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present())
      .discardingCookies(RelatedCacheKeys.RetainSet)
    // TODO remove Business Cache if consent not sent
  }
}