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
import views.vrm_retention.VehicleLookup.KeeperConsent_Business
import views.vrm_retention.RelatedCacheKeys
import scala.Some
import play.api.Logger


final class Confirm @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {

  private[controllers] val form = Form(ConfirmFormModel.Form.Mapping)

  def present = Action {
    implicit request =>
      (request.cookies.getModel[VehicleAndKeeperLookupFormModel],
        request.cookies.getModel[VehicleAndKeeperDetailsModel],
        request.cookies.getModel[BusinessDetailsModel],
        request.cookies.getString(StoreBusinessDetailsConsentCacheKey)) match {

        case (Some(vehicleAndKeeperLookupFormModel), Some(vehicleAndKeeperDetails), Some(businessDetailsModel), Some(storeBusinessDetailsConsent)) =>
          val confirmFormModel = ConfirmFormModel(None, storeBusinessDetailsConsent)
          val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails, if (vehicleAndKeeperLookupFormModel.consent == KeeperConsent_Business) Some(businessDetailsModel) else None)
          Ok(views.html.vrm_retention.confirm(confirmViewModel, form.fill(confirmFormModel)))

        case (Some(vehicleAndKeeperLookupFormModel), Some(vehicleAndKeeperDetails), Some(businessDetailsModel), None) =>
          val confirmFormModel = ConfirmFormModel(None, "")
          val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails, if (vehicleAndKeeperLookupFormModel.consent == KeeperConsent_Business) Some(businessDetailsModel) else None)
          Ok(views.html.vrm_retention.confirm(confirmViewModel, form.fill(confirmFormModel)))

        case (Some(vehicleAndKeeperLookupFormModel), Some(vehicleAndKeeperDetails), None, Some(storeBusinessDetailsConsent)) =>
          val confirmFormModel = ConfirmFormModel(None, storeBusinessDetailsConsent)
          val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails, None)
          Ok(views.html.vrm_retention.confirm(confirmViewModel, form.fill(confirmFormModel)))

        case (Some(vehicleAndKeeperLookupFormModel), Some(vehicleAndKeeperDetails), None, None) =>
          val confirmFormModel = ConfirmFormModel(None, "")
          val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails, None)
          Ok(views.html.vrm_retention.confirm(confirmViewModel, form.fill(confirmFormModel)))

        case _ =>
          Redirect(routes.VehicleLookup.present())
      }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => {
        (request.cookies.getModel[VehicleAndKeeperLookupFormModel],
          request.cookies.getModel[VehicleAndKeeperDetailsModel],
          request.cookies.getModel[BusinessDetailsModel]) match {
          case (Some(vehicleAndKeeperLookupFormModel), Some(vehicleAndKeeperDetails), Some(businessDetailsModel)) =>
            val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails,
              if (vehicleAndKeeperLookupFormModel.consent == KeeperConsent_Business) Some(businessDetailsModel) else None)
            val formWithReplacedErrors = invalidForm.
              replaceError(KeeperEmailId,
                FormError(
                  key = KeeperEmailId,
                  message = "error.validEmail",
                  args = Seq.empty)).
              distinctErrors
            BadRequest(views.html.vrm_retention.confirm(confirmViewModel, formWithReplacedErrors))
          case (Some(vehicleAndKeeperLookupFormModel), Some(vehicleAndKeeperDetails), None) =>
            val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails, None)
            val formWithReplacedErrors = invalidForm.
              replaceError(KeeperEmailId,
                FormError(
                  key = KeeperEmailId,
                  message = "error.validEmail",
                  args = Seq.empty)).
              distinctErrors
            BadRequest(views.html.vrm_retention.confirm(confirmViewModel, formWithReplacedErrors))
          case _ =>
            Redirect(routes.MicroServiceError.present())
        }
      },
      validForm => {
        (request.cookies.getModel[VehicleAndKeeperLookupFormModel]) match {
          case (Some(vehicleAndKeeperLookupFormModel)) =>
            if (vehicleAndKeeperLookupFormModel.consent == KeeperConsent_Business) {
              if (validForm.keeperEmail.isDefined) {
                Redirect(routes.Payment.present()).
                  withCookie(KeeperEmailCacheKey, validForm.keeperEmail.get).
                  withCookie(StoreBusinessDetailsConsentCacheKey, validForm.storeBusinessDetailsConsent)
              } else {
                Redirect(routes.Payment.present()).
                  withCookie(StoreBusinessDetailsConsentCacheKey, validForm.storeBusinessDetailsConsent)
              }
            } else {
              if (validForm.keeperEmail.isDefined) {
                Redirect(routes.Payment.present()).
                  withCookie(KeeperEmailCacheKey, validForm.keeperEmail.get)
              } else {
                Redirect(routes.Payment.present())
              }
            }
          case _ =>
            Redirect(routes.MicroServiceError.present())
        }
      }
    )
  }

  def exit = Action { implicit request =>
    (request.cookies.getString(StoreBusinessDetailsConsentCacheKey)) match {
      case (Some(storeBusinessDetailsConsent)) =>
        if (storeBusinessDetailsConsent == "") {
          Redirect(routes.MockFeedback.present())
            .discardingCookies(RelatedCacheKeys.RetainSet)
            .discardingCookies(RelatedCacheKeys.BusinessDetailsSet)
        } else {
          Redirect(routes.MockFeedback.present())
            .discardingCookies(RelatedCacheKeys.RetainSet)
        }
      case _ =>
        Redirect(routes.MockFeedback.present())
          .discardingCookies(RelatedCacheKeys.RetainSet)
    }
  }
}