package controllers

import audit1.AuditMessage
import com.google.inject.Inject
import models._
import play.api.Logger
import play.api.data.Form
import play.api.data.FormError
import play.api.mvc.Result
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import utils.helpers.Config
import views.vrm_retention.Confirm.KeeperEmailId
import views.vrm_retention.Confirm.SupplyEmailId
import views.vrm_retention.Confirm.SupplyEmail_true
import views.vrm_retention.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_retention.VehicleLookup.TransactionIdCacheKey
import views.vrm_retention.VehicleLookup.UserType_Business
import views.vrm_retention.VehicleLookup.UserType_Keeper
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest
import views.vrm_retention.ConfirmBusiness.StoreBusinessDetailsCacheKey

final class Confirm @Inject()(
                               auditService1: audit1.AuditService,
                               auditService2: audit2.AuditService
                               )(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                 config: Config,
                                 dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService) extends Controller {

  private[controllers] val form = Form(ConfirmFormModel.Form.Mapping)

  def present: Action[AnyContent] = Action {
    implicit request =>
      (request.cookies.getModel[VehicleAndKeeperDetailsModel],
        request.cookies.getModel[VehicleAndKeeperLookupFormModel],
        request.cookies.getModel[RetainModel],
        request.cookies.getModel[BusinessChooseYourAddressFormModel],
        request.cookies.getModel[EnterAddressManuallyModel],
        request.cookies.getString(StoreBusinessDetailsCacheKey)) match {
        case (Some(vehicleAndKeeperDetails), Some(vehicleAndKeeperLookupForm), None, businessChooseYourAddress, enterAddressManually, Some(storeBusinessDetails)) if vehicleAndKeeperLookupForm.userType == UserType_Business && (businessChooseYourAddress.isDefined || enterAddressManually.isDefined) =>
          // Happy path for a business user that has all the cookies (and they either have entered address manually)// Happy path for keeper keeper
          present(vehicleAndKeeperDetails, vehicleAndKeeperLookupForm)
        case (Some(vehicleAndKeeperDetails), Some(vehicleAndKeeperLookupForm), None, _, _, _) if vehicleAndKeeperLookupForm.userType == UserType_Keeper =>
          // Happy path for keeper keeper
          present(vehicleAndKeeperDetails, vehicleAndKeeperLookupForm)
        case _ =>
          Logger.warn("*** Confirm present is missing cookies for either keeper or business")
          Logger.warn("*** VehicleAndKeeperDetailsModel " + request.cookies.getModel[VehicleAndKeeperDetailsModel])
          Logger.warn("*** VehicleAndKeeperLookupFormModel " + request.cookies.getModel[VehicleAndKeeperLookupFormModel])
          Logger.warn("*** RetainModel " + request.cookies.getModel[RetainModel])
          Logger.warn("*** BusinessChooseYourAddressFormModel " + request.cookies.getModel[BusinessChooseYourAddressFormModel])
          Logger.warn("*** EnterAddressManuallyModel " + request.cookies.getModel[EnterAddressManuallyModel])
          Logger.warn("*** StoreBusinessDetailsCacheKey " + request.cookies.getString(StoreBusinessDetailsCacheKey))
          Redirect(routes.ConfirmBusiness.present())
      }
  }

  private def present(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel, vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel)(implicit request: Request[AnyContent]): Result = {
    val viewModel = ConfirmViewModel(vehicleAndKeeperDetails, vehicleAndKeeperLookupForm.userType)
    val emptyForm = form // Always fill the form with empty values to force user to enter new details. Also helps
    // with the situation where payment fails and they come back to this page via either back button or coming
    // forward from vehicle lookup - this could now be a different customer! We don't want the chance that one
    // customer gives up and then a new customer starts the journey in the same session and the email field is
    // pre-populated with the previous customer's address.
    val isKeeperEmailDisplayedOnLoad = false // Due to the form always being empty, the keeper email field will
    // always be hidden on first load
    val isKeeper = vehicleAndKeeperLookupForm.userType == UserType_Keeper
    Ok(views.html.vrm_retention.confirm(confirmViewModel = viewModel, confirmForm = emptyForm, isKeeperEmailDisplayedOnLoad = isKeeperEmailDisplayedOnLoad, isKeeper = isKeeper))
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => handleInvalid(invalidForm),
      model => handleValid(model)
    )
  }

  def back = Action { implicit request =>
    // If the user is a business actor, then navigate to the previous page in the business journey,
    // Else the user is a keeper actor, then navigate to the previous page in the keeper journey
    val businessPath = for {
      vehicleAndKeeperLookupForm <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
      if vehicleAndKeeperLookupForm.userType == UserType_Business
    } yield {
      Redirect(routes.ConfirmBusiness.present())
    }
    val keeperPath = Redirect(routes.VehicleLookup.present())
    businessPath.getOrElse(keeperPath)
  }

  private def formWithReplacedErrors(form: Form[ConfirmFormModel]) =
    form.
      replaceError(
        key = KeeperEmailId,
        FormError(
          key = KeeperEmailId,
          message = "error.validEmail",
          args = Seq.empty
        )
      ).replaceError(
        key = "",
        message = "email-not-supplied",
        FormError(
          key = KeeperEmailId,
          message = "email-not-supplied"
        )
      ).distinctErrors

  private def handleValid(model: ConfirmFormModel)(implicit request: Request[_]): Result = {
    val happyPath = request.cookies.getModel[VehicleAndKeeperLookupFormModel].map { vehicleAndKeeperLookup =>
      auditService1.send(AuditMessage.from(
        pageMovement = AuditMessage.ConfirmToPayment,
        timestamp = dateService.dateTimeISOChronology,
        transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
        keeperEmail = model.keeperEmail,
        businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))
      auditService2.send(AuditRequest.from(
        pageMovement = AuditMessage.ConfirmToPayment,
        timestamp = dateService.dateTimeISOChronology,
        transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
        keeperEmail = model.keeperEmail,
        businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))

      Redirect(routes.Payment.begin()).
        withCookie(model)
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
      val viewModel = ConfirmViewModel(vehicleAndKeeper, vehicleAndKeeperLookupForm.userType)
      val updatedForm = formWithReplacedErrors(form)
      val isKeeperEmailDisplayedOnLoad = updatedForm.apply(SupplyEmailId).value == Some(SupplyEmail_true)
      val isKeeper = vehicleAndKeeperLookupForm.userType == UserType_Keeper
      BadRequest(views.html.vrm_retention.confirm(viewModel, updatedForm, isKeeperEmailDisplayedOnLoad, isKeeper))
    }
    val sadPath = Redirect(routes.Error.present("user went to Confirm handleInvalid without one of the required cookies"))
    happyPath.getOrElse(sadPath)
  }

  def exit = Action { implicit request =>
    auditService1.send(AuditMessage.from(
      pageMovement = AuditMessage.ConfirmToExit,
      timestamp = dateService.dateTimeISOChronology,
      transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
      vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
      replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
      keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
      businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))
    auditService2.send(AuditRequest.from(
      pageMovement = AuditMessage.ConfirmToExit,
      timestamp = dateService.dateTimeISOChronology,
      transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
      vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
      replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
      keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
      businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))

    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }
}