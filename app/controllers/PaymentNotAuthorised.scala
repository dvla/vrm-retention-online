package controllers

import com.google.inject.Inject
import models.CacheKeyPrefix
import models.VehicleAndKeeperLookupFormModel
import models.VehicleLookupFailureViewModel
import play.api.mvc.{Action, AnyContent, Controller, Request}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichCookies
import common.model.VehicleAndKeeperDetailsModel
import common.LogFormats.DVLALogger
import utils.helpers.Config
import views.vrm_retention.VehicleLookup.TransactionIdCacheKey

final class PaymentNotAuthorised @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                             config: Config,
                                             dateService: common.services.DateService) extends Controller with DVLALogger{

  def present = Action { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel]) match {
      case (Some(transactionId), Some(vehicleAndKeeperLookupForm)) =>
        val vehicleAndKeeperDetails = request.cookies.getModel[VehicleAndKeeperDetailsModel]
        displayPaymentNotAuthorised(transactionId, vehicleAndKeeperLookupForm, vehicleAndKeeperDetails)
      case _ => Redirect(routes.BeforeYouStart.present())
    }
  }

  def submit = Action { implicit request =>
    request.cookies.getModel[VehicleAndKeeperLookupFormModel] match {
      case (Some(vehicleAndKeeperLookupFormModel)) =>
        Redirect(routes.VehicleLookup.present())
      case _ => Redirect(routes.BeforeYouStart.present())
    }
  }

  private def displayPaymentNotAuthorised(transactionId: String,
                                          vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
                                          vehicleAndKeeperDetails: Option[VehicleAndKeeperDetailsModel])
                                         (implicit request: Request[AnyContent]) = {
    val viewModel = VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm, vehicleAndKeeperDetails, failureCode = "")
    val trackingId = request.cookies.trackingId()
    logMessage(trackingId, Info, s"Presenting payment not authorised view")
    Ok(views.html.vrm_retention.payment_not_authorised(
      transactionId = transactionId,
      viewModel = viewModel,
      data = vehicleAndKeeperLookupForm
    ))
  }
}
