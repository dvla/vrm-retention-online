package controllers

import audit1.AuditMessage
import com.google.inject.Inject
import models.{EligibilityModel, SetupBusinessDetailsFormModel, SetupBusinessDetailsViewModel, VehicleAndKeeperDetailsModel}
import play.api.data.{Form, FormError}
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClearTextClientSideSessionFactory, ClientSideSessionFactory}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import utils.helpers.{Config, Config2}
import views.vrm_retention.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_retention.SetupBusinessDetails._
import views.vrm_retention.VehicleLookup._
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest

final class SetUpBusinessDetails @Inject()(
                                            auditService1: audit1.AuditService,
                                            auditService2: audit2.AuditService,
                                            dateService: DateService
                                            )
                                          (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                           config: Config,
                                           config2: Config2) extends Controller {

  private[controllers] val form = Form(
    SetupBusinessDetailsFormModel.Form.Mapping
  )

  def present = Action { implicit request =>
    request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
      case Some(vehicleAndKeeperDetails) =>
        val viewModel = SetupBusinessDetailsViewModel(vehicleAndKeeperDetails)
        Ok(views.html.vrm_retention.setup_business_details(form.fill(), viewModel))
      case _ => Redirect(routes.VehicleLookup.present())
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => {
        request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
          case Some(vehicleAndKeeperDetails) =>
            val setupBusinessDetailsViewModel = SetupBusinessDetailsViewModel(vehicleAndKeeperDetails)
            BadRequest(views.html.vrm_retention.setup_business_details(formWithReplacedErrors(invalidForm),
              setupBusinessDetailsViewModel))
          case _ =>
            Redirect(routes.VehicleLookup.present())
        }
      },
      validForm => Redirect(routes.BusinessChooseYourAddress.present()).withCookie(validForm)
    )
  }

  def exit = Action {
    implicit request =>
      auditService1.send(AuditMessage.from(
        pageMovement = AuditMessage.CaptureActorToExit,
        transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM)))
      auditService2.send(AuditRequest.from(
        pageMovement = AuditMessage.CaptureActorToExit,
        transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM)))

      Redirect(routes.LeaveFeedback.present()).
        discardingCookies(removeCookiesOnExit)
  }

  private def formWithReplacedErrors(form: Form[SetupBusinessDetailsFormModel])(implicit request: Request[_]) =
    (form /: List(
      (BusinessNameId, "error.validBusinessName"),
      (BusinessContactId, "error.validBusinessContact"),
      (BusinessEmailId, "error.validEmail"),
      (BusinessPostcodeId, "error.restricted.validPostcode"))) { (form, error) =>
      form.replaceError(error._1, FormError(
        key = error._1,
        message = error._2,
        args = Seq.empty
      ))
    }.distinctErrors
}
