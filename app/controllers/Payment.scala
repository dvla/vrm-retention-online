package controllers

import com.google.inject.Inject
import play.api.Logger
import play.api.mvc.{Result, _}
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import models.VehicleAndKeeperLookupFormModel
import views.vrm_retention.Confirm._
import views.vrm_retention.Payment._
import views.vrm_retention.RelatedCacheKeys
import views.vrm_retention.VehicleLookup._
import webserviceclients.paymentsolve.{PaymentSolveBeginRequest, PaymentSolveGetRequest, PaymentSolveService}
import webserviceclients.vrmretentionretain.VRMRetentionRetainService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

final class Payment @Inject()(vrmRetentionRetainService: VRMRetentionRetainService,
                              paymentSolveService: PaymentSolveService,
                              dateService: DateService)
                             (implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {

  private val VALIDATED_RESPONSE = "validated"
  private val AUTHORISED_STATUS = "AUTHORISED"

  def begin = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey), request.cookies.getModel[VehicleAndKeeperLookupFormModel]) match {
      case (Some(transactionId), Some(vehiclesLookupForm)) =>
        callBeginWebPaymentService(transactionId, vehiclesLookupForm.registrationNumber)
      case _ => Future.successful {
        Redirect(routes.MicroServiceError.present()) // TODO is this the correct redirect?
      }
    }
  }

  def paymentCallback = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey), request.cookies.getString(PaymentTransactionReferenceCacheKey)) match {
      case (Some(transactionId), Some(trxRef)) =>
        callGetWebPaymentService(transactionId, trxRef)
      case _ => Future.successful {
        Redirect(routes.MicroServiceError.present()) // TODO is this the correct redirect?
      }
    }
  }

  def submit = Action.async { implicit request =>
    ??? // TODO
  }

  def exit = Action { implicit request =>
    if (request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)) {
      Redirect(routes.MockFeedback.present())
        .discardingCookies(RelatedCacheKeys.RetainSet)
    } else {
      Redirect(routes.MockFeedback.present())
        .discardingCookies(RelatedCacheKeys.RetainSet)
        .discardingCookies(RelatedCacheKeys.BusinessDetailsSet)
    }
  }

  private def callBeginWebPaymentService(transactionId: String, vrm: String)(implicit request: Request[_]): Future[Result] = {

    def paymentBeginFailure = {
      Logger.debug(s"Payment Solve encountered a problem with request ${LogFormats.anonymize(vrm)}, redirect to PaymentFailure")
      Redirect(routes.PaymentFailure.present())
    }

    val paymentSolveBeginRequest = PaymentSolveBeginRequest(
      transNo = transactionId.replaceAll("[^0-9]", ""), // TODO find a suitable trans no
      vrm = vrm,
      paymentCallback = routes.Payment.paymentCallback().absoluteURL()
    )
    val trackingId = request.cookies.trackingId()

    paymentSolveService.invoke(paymentSolveBeginRequest, trackingId).map { response =>
      if (response.response == VALIDATED_RESPONSE) {
        //        Redirect(new Call("GET", response.redirectUrl.get)) // TODO call this when csrf problem resolved
        Redirect(routes.Payment.paymentCallback()).withCookie(PaymentTransactionReferenceCacheKey, response.trxRef.get)
      } else {
        Logger.error("The begin web request to Solve was not validated.")
        paymentBeginFailure
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Payment Solve Web service call failed. Exception " + e.toString.take(45))
        paymentBeginFailure
    }
  }

  private def callGetWebPaymentService(transactionId: String, trxRef: String)
                                      (implicit request: Request[_]): Future[Result] = {

    def paymentGetFailure = {
      Logger.debug(s"Payment Solve encountered a problem with request ${LogFormats.anonymize(trxRef)}, redirect to PaymentFailure")
      Redirect(routes.PaymentFailure.present())
    }

    def paymentNotAuthorised = {
      Logger.debug(s"Payment not authorised for ${LogFormats.anonymize(trxRef)}, redirect to PaymentNotAuthorised")
      Redirect(routes.PaymentNotAuthorised.present())
    }

    val paymentSolveGetRequest = PaymentSolveGetRequest(
      transNo = transactionId.replaceAll("[^0-9]", ""), // TODO find a suitable trans no
      trxRef = trxRef
    )
    val trackingId = request.cookies.trackingId()

    paymentSolveService.invoke(paymentSolveGetRequest, trackingId).map { response =>
      if (response.response == VALIDATED_RESPONSE) {
        // TODO store the auth code and masked pan
        //          if (response.status == AUTHORISED_STATUS) { // TODO because we don't call Solve , because of csrf, the AUTHORISED status is NOT_AUTHORISED so ignore for now
        Redirect(routes.Retain.submit())
        //          } else {
        //            Logger.debug("The payment was not authorised.")
        //            paymentNotAuthorised
        //          }
      } else {
        Logger.error("The get web request to Solve was not validated.")
        paymentGetFailure
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Payment Solve Web service call failed. Exception " + e.toString.take(245))
        paymentGetFailure
    }
  }
}
