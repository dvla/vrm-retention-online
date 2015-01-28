package controllers

import com.google.inject.Inject
import models.{PaymentModel, VehicleAndKeeperLookupFormModel, VehicleLookupFailureViewModel}
import play.api.Logger
import play.api.mvc.{Result, _}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config2
import views.vrm_retention.VehicleLookup._
import webserviceclients.paymentsolve.{PaymentSolveCancelRequest, PaymentSolveService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

final class RetainFailure @Inject()(paymentSolveService: PaymentSolveService)
                                   (implicit clientSideSessionFactory: ClientSideSessionFactory,

                                    config2: Config2) extends Controller {

  def present = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[PaymentModel],
      request.cookies.getModel[VehicleAndKeeperLookupFormModel]) match {

      case (Some(transactionId), Some(paymentModel), Some(vehicleAndKeeperLookupFormModel)) =>
        val vehicleAndKeeperDetails = request.cookies.getModel[VehicleAndKeeperDetailsModel]
        callCancelWebPaymentService(transactionId, paymentModel.trxRef.get, vehicleAndKeeperLookupFormModel, vehicleAndKeeperDetails)
      case _ =>
        Future.successful(Redirect(routes.MicroServiceError.present()))
    }
  }

  private def callCancelWebPaymentService(transactionId: String, trxRef: String,
                                          vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
                                          vehicleAndKeeperDetails: Option[VehicleAndKeeperDetailsModel])
                                         (implicit request: Request[_]): Future[Result] = {

    val paymentSolveCancelRequest = PaymentSolveCancelRequest(
      transNo = transactionId.replaceAll("[^0-9]", ""),
      trxRef = trxRef
    )
    val trackingId = request.cookies.trackingId()

    paymentSolveService.invoke(paymentSolveCancelRequest, trackingId).map {
      response =>
        val viewModel = vehicleAndKeeperDetails match {
          case Some(details) => VehicleLookupFailureViewModel(details)
          case None => VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm)
        }
        Ok(views.html.vrm_retention.retention_failure(
          transactionId = transactionId,
          vehicleLookupFailureViewModel = viewModel))
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"RetainFailure Payment Solve web service call with paymentSolveCancelRequest failed. Exception " + e.toString)
        val viewModel = vehicleAndKeeperDetails match {
          case Some(details) => VehicleLookupFailureViewModel(details)
          case None => VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm)
        }
        Ok(views.html.vrm_retention.retention_failure(
          transactionId = transactionId,
          vehicleLookupFailureViewModel = viewModel))
    }
  }
}