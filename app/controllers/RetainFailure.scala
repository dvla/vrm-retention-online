package controllers

import com.google.inject.Inject
import models.{CacheKeyPrefix, PaymentModel, VehicleLookupFailureViewModel, VehicleAndKeeperLookupFormModel}
import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.model.{MicroserviceResponseModel, VehicleAndKeeperDetailsModel}
import utils.helpers.Config
import views.vrm_retention.VehicleLookup.TransactionIdCacheKey
import webserviceclients.paymentsolve.PaymentSolveService

final class RetainFailure @Inject()(paymentSolveService: PaymentSolveService)
                                   (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config,
                                    dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService
                                   ) extends Controller with DVLALogger {

  def present = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[PaymentModel],
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getModel[MicroserviceResponseModel]) match {

      case (Some(transactionId),
            Some(paymentModel),
            Some(vehicleAndKeeperLookupFormModel),
            vehicleAndKeeperDetailsModelOpt,
            Some(ms)) =>

      val viewModel = VehicleLookupFailureViewModel(
        vehicleAndKeeperLookupFormModel,
        vehicleAndKeeperDetailsModelOpt,
        ms.msResponse.code
      )

      logMessage(request.cookies.trackingId(), Info, s"Presenting retention failure view")
      Future.successful(Ok(views.html.vrm_retention.retention_failure(
        transactionId = transactionId,
        viewModel = viewModel)))

      case _ =>
        Future.successful(Redirect(routes.MicroServiceError.present()))
    }
  }
}
