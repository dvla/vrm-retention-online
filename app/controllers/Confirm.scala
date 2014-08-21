package controllers

import com.google.inject.Inject
import play.api.data.{Form, FormError}
import play.api.Logger
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
          Logger.debug("case 1 " + storeBusinessDetailsConsent)

          val confirmFormModel = ConfirmFormModel(None, storeBusinessDetailsConsent)
          val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails, businessDetailsModel)
          Ok(views.html.vrm_retention.confirm(confirmViewModel, form.fill(confirmFormModel)))

        case (Some(vehicleAndKeeperDetails), Some(businessDetailsModel), None) =>
          Logger.debug("case 2 ")

          val confirmFormModel = ConfirmFormModel(None, "")
          val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails, businessDetailsModel)
          Ok(views.html.vrm_retention.confirm(confirmViewModel, form.fill(confirmFormModel)))

        case (Some(vehicleAndKeeperDetails), None, Some(storeBusinessDetailsConsent)) =>
          Logger.debug("case 3 " + storeBusinessDetailsConsent)

          val confirmFormModel = ConfirmFormModel(None, storeBusinessDetailsConsent)
          val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails)
          Ok(views.html.vrm_retention.confirm(confirmViewModel, form.fill(confirmFormModel)))

        case (Some(vehicleAndKeeperDetails), None, None) =>
          Logger.debug("case 4 ")

          val confirmFormModel = ConfirmFormModel(None, "")
          val confirmViewModel = ConfirmViewModel(vehicleAndKeeperDetails)
          Ok(views.html.vrm_retention.confirm(confirmViewModel, form.fill(confirmFormModel)))

        case _ =>
          Redirect(routes.VehicleLookup.present())
      }
  }

  def submit = Action { implicit request =>

    Logger.debug("submit ")

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

        Logger.debug("validForm " + validForm.storeBusinessDetailsConsent)

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

    Logger.debug("confirm exit 1")

    (request.cookies.getString(StoreBusinessDetailsConsentCacheKey)) match {
      case (Some(storeBusinessDetailsConsent)) =>
        Logger.debug("confirm exit 2")

        if (storeBusinessDetailsConsent == "") {
          Redirect(routes.MockFeedback.present())
            .discardingCookies(RelatedCacheKeys.RetainSet)
            .discardingCookies(RelatedCacheKeys.BusinessDetailsSet)
        } else {
          Redirect(routes.MockFeedback.present())
            .discardingCookies(RelatedCacheKeys.RetainSet)
        }
      case _ =>
        Logger.debug("confirm exit 3")

        Redirect(routes.MockFeedback.present())
          .discardingCookies(RelatedCacheKeys.RetainSet)
    }
  }
}