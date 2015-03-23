package controllers

import audit1._
import com.google.inject.Inject
import models._
import play.api.data.Form
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_retention.ConfirmBusiness._
import views.vrm_retention.EnterAddressManually.EnterAddressManuallyCacheKey
import views.vrm_retention.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_retention.VehicleLookup._
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest

final class ConfirmBusiness @Inject()(
                                       auditService1: audit1.AuditService,
                                       auditService2: audit2.AuditService
                                       )(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                         config: Config,
                                         dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService) extends Controller {

  private[controllers] val form = Form(ConfirmBusinessFormModel.Form.Mapping)

  def present = Action { implicit request => {
    val happyPath = for {
      vehicleAndKeeperLookupForm <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
      vehicleAndKeeper <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
      setupBusinessDetailsFormModel <- request.cookies.getModel[SetupBusinessDetailsFormModel]
      businessDetailsModel <- request.cookies.getModel[BusinessDetailsModel]
    } yield {
        val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)
        val isBusinessUser = vehicleAndKeeperLookupForm.userType == UserType_Business
        val verifiedBusinessDetails = request.cookies.getModel[BusinessDetailsModel].filter(o => isBusinessUser)
        val formModel = ConfirmBusinessFormModel(storeBusinessDetails)
        val viewModel = ConfirmBusinessViewModel(vehicleAndKeeper, verifiedBusinessDetails)
        Ok(views.html.vrm_retention.confirm_business(viewModel, form.fill(formModel)))
      }
    val sadPath = Redirect(routes.BusinessChooseYourAddress.present())
    happyPath.getOrElse(sadPath)
  }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => handleInvalid(invalidForm),
      model => handleValid(model)
    )
  }

  def back = Action { implicit request =>
    request.cookies.getModel[EnterAddressManuallyModel] match {
      case Some(enterAddressManuallyModel) => Redirect(routes.EnterAddressManually.present()) // The last page was EnterAddressManually so go there.
      case None => Redirect(routes.BusinessChooseYourAddress.present())
    }
  }

  private def handleValid(model: ConfirmBusinessFormModel)(implicit request: Request[_]): Result = {
    val happyPath = for {
      vehicleAndKeeperLookup <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
    } yield {
        (request.cookies.getString(TransactionIdCacheKey),
          request.cookies.getModel[VehicleAndKeeperDetailsModel],
          request.cookies.getModel[EligibilityModel],
          request.cookies.getModel[BusinessDetailsModel],
          request.cookies.getModel[EnterAddressManuallyModel],
          request.cookies.getModel[BusinessChooseYourAddressFormModel],
          request.cookies.getModel[SetupBusinessDetailsFormModel]
          ) match {
          case (transactionId, vehicleAndKeeperDetailsModel, eligibilityModel, businessDetailsModel, enterAddressManuallyModel, businessChooseYourAddressFormModel, setupBusinessDetailsFormModel) =>

        auditService1.send(AuditMessage.from(
          pageMovement = AuditMessage.ConfirmBusinessToConfirm,
          transactionId = transactionId.getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = vehicleAndKeeperDetailsModel,
          replacementVrm = Some(eligibilityModel.get.replacementVRM),
          businessDetailsModel = businessDetailsModel))
        auditService2.send(AuditRequest.from(
          pageMovement = AuditMessage.ConfirmBusinessToConfirm,
          transactionId = transactionId.getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = vehicleAndKeeperDetailsModel,
          replacementVrm = Some(eligibilityModel.get.replacementVRM),
          businessDetailsModel = businessDetailsModel))

        Redirect(routes.Confirm.present()).
          withCookie(enterAddressManuallyModel).
          withCookie(businessChooseYourAddressFormModel).
          withCookie(businessDetailsModel).
          withCookie(StoreBusinessDetailsCacheKey, model.storeBusinessDetails.toString).
          withCookie(setupBusinessDetailsFormModel)
        }
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
        val formModel = ConfirmBusinessFormModel(storeBusinessDetails)
        BadRequest(views.html.vrm_retention.confirm_business(viewModel, form.fill(formModel)))
      }
    val sadPath = Redirect(routes.Error.present("user went to Confirm handleInvalid without one of the required cookies"))
    happyPath.getOrElse(sadPath)
  }

  def exit = Action { implicit request =>
    auditService1.send(AuditMessage.from(
      pageMovement = AuditMessage.ConfirmBusinessToExit,
      transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
      timestamp = dateService.dateTimeISOChronology,
      vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
      replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
      businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))
    auditService2.send(AuditRequest.from(
      pageMovement = AuditMessage.ConfirmBusinessToExit,
      transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
      timestamp = dateService.dateTimeISOChronology,
      vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
      replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
      businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))

    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }
}