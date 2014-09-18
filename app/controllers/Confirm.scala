package controllers

import com.google.inject.Inject
import models._
import play.api.data.{Form, FormError}
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClientSideSessionFactory, CookieKeyValue}
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import utils.helpers.Config
import views.vrm_retention.Confirm._
import views.vrm_retention.RelatedCacheKeys
import views.vrm_retention.VehicleLookup.UserType_Business

final class Confirm @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {

  private[controllers] val form = Form(ConfirmFormModel.Form.Mapping)

  def present = Action(implicit request => {
    val happyPath = for {
      vehicleAndKeeperLookupForm <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
      vehicleAndKeeper <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
    } yield {
      val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)
      val isBusinessUser = vehicleAndKeeperLookupForm.userType == UserType_Business
      val verifiedBusinessDetails = request.cookies.getModel[BusinessDetailsModel].filter(o => isBusinessUser)
      val showStoreDetails = storeBusinessDetails && isBusinessUser
      val formModel = ConfirmFormModel(None, showStoreDetails)
      val viewModel = ConfirmViewModel(vehicleAndKeeper, verifiedBusinessDetails)
      Ok(views.html.vrm_retention.confirm(viewModel, form.fill(formModel)))
    }
    val sadPath = Redirect(routes.VehicleLookup.present())
    happyPath.getOrElse(sadPath)
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
    val happyPath = request.cookies.getModel[VehicleAndKeeperLookupFormModel].map { vehicleAndKeeperLookup =>
      val keeperEmail = model.keeperEmail.map(CookieKeyValue(KeeperEmailCacheKey, _))
      val storeBusinessDetails =
        if (vehicleAndKeeperLookup.userType == UserType_Business)
          Some(CookieKeyValue(StoreBusinessDetailsCacheKey, model.storeBusinessDetails.toString))
        else
          None

      val cookies = List(keeperEmail, storeBusinessDetails).flatten
      Redirect(routes.Payment.begin()).withCookiesEx(cookies: _*)
    }
    val sadPath = Redirect(routes.MicroServiceError.present())
    happyPath.getOrElse(sadPath)
  }

  private def handleInvalid(form: Form[ConfirmFormModel])(implicit request: Request[_]): Result = {
    val happyPath = for {
      vehicleAndKeeperLookupForm <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
      vehicleAndKeeper <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
    }
    yield {
      val businessDetails = request.cookies.getModel[BusinessDetailsModel]
      val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)
      val viewModel = ConfirmViewModel(vehicleAndKeeper, businessDetails.filter(details => storeBusinessDetails))
      val updatedForm = replaceErrorMsg(form, KeeperEmailId, "error.validEmail").distinctErrors
      BadRequest(views.html.vrm_retention.confirm(viewModel, updatedForm))
    }
    val sadPath = Redirect(routes.MicroServiceError.present())
    happyPath.getOrElse(sadPath)
  }

  def exit = Action { implicit request =>
    val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)
    val cacheKeys = RelatedCacheKeys.RetainSet ++ {
      if (storeBusinessDetails) Set.empty else RelatedCacheKeys.BusinessDetailsSet
    }
    Redirect(routes.MockFeedback.present()).discardingCookies(cacheKeys)
  }
}