package controllers

import com.google.inject.Inject
import models._
import views.vrm_retention.CheckEligibility.CheckEligibilityCacheKey
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{CookieKeyValue, ClientSideSessionFactory}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import utils.helpers.Config
import views.vrm_retention.RelatedCacheKeys
import views.vrm_retention.VehicleLookup._
import audit.{ConfirmToPaymentAuditMessage, ConfirmBusinessToConfirmAuditMessage, AuditService}
import views.vrm_retention.ConfirmBusiness._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieKeyValue
import scala.Some
import play.api.data.{FormError, Form}
import views.vrm_retention.Confirm._
import play.api.mvc.Result
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieKeyValue
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

final class ConfirmBusiness @Inject()(auditService: AuditService)(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                                                  config: Config) extends Controller {

  private[controllers] val form = Form(ConfirmBusinessFormModel.Form.Mapping)

  def present = Action(implicit request => {
    val happyPath = for {
      vehicleAndKeeperLookupForm <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
      vehicleAndKeeper <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
    } yield {
      val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)
      val isBusinessUser = vehicleAndKeeperLookupForm.userType == UserType_Business
      val verifiedBusinessDetails = request.cookies.getModel[BusinessDetailsModel].filter(o => isBusinessUser)
      //      val showStoreDetails = storeBusinessDetails && isBusinessUser
      val formModel = ConfirmBusinessFormModel(storeBusinessDetails)
      val viewModel = ConfirmBusinessViewModel(vehicleAndKeeper, verifiedBusinessDetails)
      Ok(views.html.vrm_retention.confirm_business(viewModel, form.fill(formModel)))
    }
    val sadPath = Redirect(routes.VehicleLookup.present())
    happyPath.getOrElse(sadPath)
  })

  def submit = Action {
    implicit request =>
      form.bindFromRequest.fold(
        invalidForm => handleInvalid(invalidForm),
        model => handleValid(model)
      )
  }

  // TODO need to remove this as it's a copy and paste from Confirm
  private def replaceErrorMsg(form: Form[ConfirmBusinessFormModel], id: String, msgId: String) =
    form.replaceError(
      KeeperEmailId,
      FormError(
        key = id,
        message = msgId,
        args = Seq.empty
      )
    )

  private def handleValid(model: ConfirmBusinessFormModel)(implicit request: Request[_]): Result = {
    val happyPath = request.cookies.getModel[VehicleAndKeeperLookupFormModel].map {
      vehicleAndKeeperLookup =>

        val storeBusinessDetails =
          if (vehicleAndKeeperLookup.userType == UserType_Business)
            Some(CookieKeyValue(StoreBusinessDetailsCacheKey, model.storeBusinessDetails.toString))
          else
            None

        val cookies = List(storeBusinessDetails).flatten

        // retrieve audit values not already in scope
        val vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel].get
        val transactionId = request.cookies.getString(TransactionIdCacheKey).get
        val replacementVRM = request.cookies.getString(CheckEligibilityCacheKey).get

        auditService.send(ConfirmBusinessToConfirmAuditMessage.from(transactionId,
          vehicleAndKeeperLookup, vehicleAndKeeperDetailsModel, vehicleAndKeeperDetailsModel.registrationNumber,
          replacementVRM))

        Redirect(routes.Confirm.present()).withCookiesEx(cookies: _*)
    }
    val sadPath = Redirect(routes.Error.present("user went to ConfirmBusiness handleValid without VehicleAndKeeperLookupFormModel cookie"))
    happyPath.getOrElse(sadPath)
  }

  private def handleInvalid(form: Form[ConfirmBusinessFormModel])(implicit request: Request[_]): Result = {
    val happyPath = for {
      vehicleAndKeeperLookupForm <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
      vehicleAndKeeper <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
    }
    yield {
      val businessDetails = request.cookies.getModel[BusinessDetailsModel]
      val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)
      val viewModel = ConfirmBusinessViewModel(vehicleAndKeeper, businessDetails.filter(details => storeBusinessDetails))
      val updatedForm = replaceErrorMsg(form, KeeperEmailId, "error.validEmail").distinctErrors
      BadRequest(views.html.vrm_retention.confirm_business(viewModel, updatedForm))
    }
    val sadPath = Redirect(routes.Error.present("user went to Confirm handleInvalid without one of the required cookies"))
    happyPath.getOrElse(sadPath)
  }

  def exit = Action {
    implicit request =>
      val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)
      val cacheKeys = RelatedCacheKeys.RetainSet ++ {
        if (storeBusinessDetails) Set.empty else RelatedCacheKeys.BusinessDetailsSet
      }
      Redirect(routes.MockFeedback.present()).discardingCookies(cacheKeys)
  }
}