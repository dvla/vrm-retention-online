package controllers

import com.google.inject.Inject
import models.BusinessDetailsModel
import models.CacheKeyPrefix
import models.ConfirmBusinessViewModel
import models.EligibilityModel
import models.RetainModel
import models.SetupBusinessDetailsFormModel
import models.VehicleAndKeeperLookupFormModel
import play.api.mvc.{Action, Controller, Request, Result}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_retention.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_retention.VehicleLookup.{TransactionIdCacheKey, UserType_Business}
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest

final class ConfirmBusiness @Inject()(auditService2: audit2.AuditService)
                                     (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config,
                                       dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService
                                     ) extends Controller {

  def present = Action { implicit request => {
      val happyPath =
        for {
          vehicleAndKeeperLookupForm <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
          vehicleAndKeeper <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
          setupBusinessDetailsFormModel <- request.cookies.getModel[SetupBusinessDetailsFormModel]
          businessDetailsModel <- request.cookies.getModel[BusinessDetailsModel]
        } yield {
          val isBusinessUser = vehicleAndKeeperLookupForm.userType == UserType_Business
          val verifiedBusinessDetails = request.cookies.getModel[BusinessDetailsModel].filter(o => isBusinessUser)
          val viewModel = ConfirmBusinessViewModel(vehicleAndKeeper, verifiedBusinessDetails)
          Ok(views.html.vrm_retention.confirm_business(viewModel))
        }
      val sadPath = Redirect(routes.SetUpBusinessDetails.present())

      // explicit check to find out if we are coming from the back browser button after we completed the transaction
      // completing the transaction will create the RetainModel, and in that case we don't want to land here but go
      // back to the start.
      if ( request.cookies.getModel[RetainModel].isDefined) {
        Redirect(routes.VehicleLookup.present())
      } else {
        happyPath.getOrElse(sadPath)
      }
    }
  }

  def submit = Action { implicit request =>
    handleValid()
  }

  def back = Action { implicit request =>
    Redirect(routes.SetUpBusinessDetails.present())
  }

  private def handleValid()(implicit request: Request[_]): Result = {
    val happyPath =
      for {
        vehicleAndKeeperLookup <- request.cookies.getModel[VehicleAndKeeperLookupFormModel]
      } yield {
        (request.cookies.getString(TransactionIdCacheKey),
          request.cookies.getModel[VehicleAndKeeperDetailsModel],
          request.cookies.getModel[EligibilityModel],
          request.cookies.getModel[BusinessDetailsModel],
          request.cookies.getModel[SetupBusinessDetailsFormModel]
          ) match {
          case (transactionId,
            vehicleAndKeeperDetailsModel,
            eligibilityModel,
            businessDetailsModel,
            setupBusinessDetailsFormModel) =>

            auditService2.send(AuditRequest.from(
              trackingId = request.cookies.trackingId,
              pageMovement = AuditRequest.ConfirmBusinessToConfirm,
              transactionId = transactionId.getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId.value),
              timestamp = dateService.dateTimeISOChronology,
              vehicleAndKeeperDetailsModel = vehicleAndKeeperDetailsModel,
              replacementVrm = Some(eligibilityModel.get.replacementVRM),
              businessDetailsModel = businessDetailsModel))
            Redirect(routes.Confirm.present())
              .withCookie(businessDetailsModel)
              .withCookie(setupBusinessDetailsFormModel)
        }
      }
    val msg = "user went to ConfirmBusiness handleValid without VehicleAndKeeperLookupFormModel cookie"
    val sadPath = Redirect(routes.Error.present(msg))
    happyPath.getOrElse(sadPath)
  }

  def exit = Action { implicit request =>
    auditService2.send(AuditRequest.from(
      trackingId = request.cookies.trackingId,
      pageMovement = AuditRequest.ConfirmBusinessToExit,
      transactionId = request.cookies.getString(TransactionIdCacheKey)
        .getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId.value),
      timestamp = dateService.dateTimeISOChronology,
      vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
      replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
      businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))

    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }
}