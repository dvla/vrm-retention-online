package controllers

import com.google.inject.Inject
import models.{CacheKeyPrefix, PaymentModel, VehicleLookupFailureViewModel, VehicleAndKeeperLookupFormModel}
import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_retention.VehicleLookup.TransactionIdCacheKey
import webserviceclients.paymentsolve.PaymentSolveService

final class RetainFailure @Inject()(paymentSolveService: PaymentSolveService)
                                   (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config,
                                    dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService
                                   ) extends Controller {

  def present = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[PaymentModel],
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[VehicleAndKeeperDetailsModel]) match {

      case (Some(transactionId),
            Some(paymentModel),
            Some(vehicleAndKeeperLookupFormModel),
            vehicleAndKeeperDetailsModelOpt) =>

        val viewModel = vehicleAndKeeperDetailsModelOpt match {
          case Some(details) => VehicleLookupFailureViewModel(details)
          case None => VehicleLookupFailureViewModel(vehicleAndKeeperLookupFormModel)
        }

        Future.successful(Ok(views.html.vrm_retention.retention_failure(
          transactionId = transactionId,
          vehicleLookupFailureViewModel = viewModel)))

      case _ =>
        Future.successful(Redirect(routes.MicroServiceError.present()))
    }
  }
}