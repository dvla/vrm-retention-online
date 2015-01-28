package controllers

import audit1._
import com.google.inject.Inject
import models._
import play.api.data.{Form, FormError}
import play.api.mvc.{Result, _}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClearTextClientSideSessionFactory, ClientSideSessionFactory, CookieKeyValue}
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import utils.helpers.Config
import views.vrm_retention.Confirm._
import views.vrm_retention.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_retention.VehicleLookup._
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest

final class Confirm @Inject()(
                               auditService1: audit1.AuditService,
                               auditService2: audit2.AuditService,
                               dateService: DateService
                               )(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                 config2: Config) extends Controller {

  private[controllers] val form = Form(ConfirmFormModel.Form.Mapping)

  def present = Action { implicit request =>
    val happyPath = for {
      vehicleAndKeeperLookupForm <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
      vehicleAndKeeper <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
    } yield {
      val formModel = ConfirmFormModel(None)
      val viewModel = ConfirmViewModel(vehicleAndKeeper, vehicleAndKeeperLookupForm.userType)
      Ok(views.html.vrm_retention.confirm(viewModel, form.fill(formModel)))
    }
    val sadPath = Redirect(routes.VehicleLookup.present())
    happyPath.getOrElse(sadPath)
  }

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
      val viewModel = ConfirmViewModel(vehicleAndKeeper, vehicleAndKeeperLookupForm.userType)
      val updatedForm = replaceErrorMsg(form, KeeperEmailId, "error.validEmail").distinctErrors
      BadRequest(views.html.vrm_retention.confirm(viewModel, updatedForm))
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
      keeperEmail = request.cookies.getString(KeeperEmailCacheKey),
      businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))
    auditService2.send(AuditRequest.from(
      pageMovement = AuditMessage.ConfirmToExit,
      timestamp = dateService.dateTimeISOChronology,
      transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
      vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
      replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
      keeperEmail = request.cookies.getString(KeeperEmailCacheKey),
      businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))

    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }
}