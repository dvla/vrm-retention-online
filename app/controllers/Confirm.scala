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
import views.vrm_retention.VehicleLookup.UserType_Business
import views.vrm_retention.RelatedCacheKeys


final class Confirm @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {

  private[controllers] val form = Form(ConfirmFormModel.Form.Mapping)

  def present = Action(implicit request => {
    val happyPath = for {
      vehicleAndKeeperLookupForm <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
      vehicleAndKeeper <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
    } yield {
      val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).map(_.toBoolean).getOrElse(false)
      val businessUser = vehicleAndKeeperLookupForm.userType == UserType_Business
      val verifiedBusinessDetails = request.cookies.getModel[BusinessDetailsModel].filter(o => businessUser)
      val showStoreDetails = storeBusinessDetails && businessUser
      val formModel = ConfirmFormModel(None, showStoreDetails)
      val viewModel = ConfirmViewModel(vehicleAndKeeper, verifiedBusinessDetails)
      Ok(views.html.vrm_retention.confirm(viewModel, form.fill(formModel)))
    }

    happyPath.getOrElse(Redirect(routes.VehicleLookup.present()))
  })

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => handleInvalid(invalidForm),
      model => handleValid(model)
    )
  }

  private def replaceErrorMsg(form: Form[ConfirmFormModel], id: String, msgId: String) =
    form.replaceError(
      KeeperEmailId,
      FormError(
        key = id,
        message = msgId,
        args = Seq.empty
      )
    )

  private def handleValid(model: ConfirmFormModel)(implicit request: Request[_]): Result = {
    val storedBusinessDetails = model.storeBusinessDetails.toString
    request.cookies.getModel[VehicleAndKeeperLookupFormModel] match {
      case Some(vehicleAndKeeperLookup) if (vehicleAndKeeperLookup.userType == UserType_Business) =>
        model.keeperEmail.fold {
          Redirect(routes.Payment.present()).
            withCookie(StoreBusinessDetailsCacheKey, storedBusinessDetails)
        } { email =>
          Redirect(routes.Payment.present()).
            withCookie(KeeperEmailCacheKey, email).
            withCookie(StoreBusinessDetailsCacheKey, storedBusinessDetails)
        }
      case Some(vehicleAndKeeperLookup) =>
        model.keeperEmail.fold {
          Redirect(routes.Payment.present())
        } { email =>
          Redirect(routes.Payment.present()).
          withCookie(KeeperEmailCacheKey, email)
        }
      case _ =>
        Redirect(routes.MicroServiceError.present())
    }
  }

  private def handleInvalid(form: Form[ConfirmFormModel])(implicit request: Request[_]): Result = {
    (request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getModel[BusinessDetailsModel],
      request.cookies.getString(StoreBusinessDetailsCacheKey).map(_.toBoolean).getOrElse(false)) match {
        case (Some(vehicleAndKeeperLookupForm),
              Some(vehicleAndKeeper),
              businessDetailsOpt,
              storeBusinessDetails) =>
          val viewModel = ConfirmViewModel(vehicleAndKeeper, businessDetailsOpt.filter(x => storeBusinessDetails))
          val updatedForm = replaceErrorMsg(form, KeeperEmailId, "error.validEmail").distinctErrors
          BadRequest(views.html.vrm_retention.confirm(viewModel, updatedForm))
        case _ =>
          Redirect(routes.MicroServiceError.present())
    }
  }

  def exit = Action { implicit request =>
    if (request.cookies.getString(StoreBusinessDetailsCacheKey).map(_.toBoolean).getOrElse(false)) {
      Redirect(routes.MockFeedback.present())
        .discardingCookies(RelatedCacheKeys.RetainSet)
    } else {
      Redirect(routes.MockFeedback.present())
        .discardingCookies(RelatedCacheKeys.RetainSet)
        .discardingCookies(RelatedCacheKeys.BusinessDetailsSet)
    }
  }
}
