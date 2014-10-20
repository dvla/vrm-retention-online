package controllers

import com.google.inject.Inject
import models._
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClientSideSessionFactory, CookieKeyValue}
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import utils.helpers.Config
import views.vrm_retention.Confirm._
import views.vrm_retention.ConfirmBusiness._
import views.vrm_retention.RelatedCacheKeys
import views.vrm_retention.VehicleLookup._
import audit.{ConfirmToPaymentAuditMessage, AuditService}
import scala.Some
import play.api.mvc.Result
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieKeyValue
import views.vrm_retention.CheckEligibility._
import scala.Some
import play.api.mvc.Result
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieKeyValue

final class Confirm @Inject()(auditService: AuditService)(implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {

  private[controllers] val form = Form(ConfirmFormModel.Form.Mapping)

  def present = Action(implicit request => {
    val happyPath = for {
      vehicleAndKeeperLookupForm <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
      vehicleAndKeeper <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
    } yield {
      val isBusinessUser = vehicleAndKeeperLookupForm.userType == UserType_Business // TODO do we need this and the next line after splitting confirm up?
      val verifiedBusinessDetails = request.cookies.getModel[BusinessDetailsModel].filter(o => isBusinessUser)
      val formModel = ConfirmFormModel(None)
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

      val cookies = List(keeperEmail).flatten

      // retrieve audit values not already in scope
      val vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel].get
      val transactionId = request.cookies.getString(TransactionIdCacheKey).get
      val replacementVRM = request.cookies.getString(CheckEligibilityCacheKey).get

      auditService.send(ConfirmToPaymentAuditMessage.from(
        vehicleAndKeeperLookup, vehicleAndKeeperDetailsModel, transactionId, vehicleAndKeeperDetailsModel.registrationNumber,
        replacementVRM, model.keeperEmail))

      Redirect(routes.Payment.begin()).withCookiesEx(cookies: _*)
    }
    val sadPath = Redirect(routes.Error.present("user went to Confirm handleValid without VehicleAndKeeperLookupFormModel cookie"))
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
    val sadPath = Redirect(routes.Error.present("user went to Confirm handleInvalid without one of the required cookies"))
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