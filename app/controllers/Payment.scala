package controllers

import com.google.inject.Inject
import models.VehicleAndKeeperLookupFormModel
import play.api.Logger
import play.api.mvc.{Action, Controller, Request, Result}
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import views.vrm_retention.Confirm._
import views.vrm_retention.Payment._
import views.vrm_retention.RelatedCacheKeys
import views.vrm_retention.VehicleLookup._
import webserviceclients.paymentsolve.{PaymentSolveBeginRequest, PaymentSolveCancelRequest, PaymentSolveGetRequest, PaymentSolveService}
import webserviceclients.vrmretentionretain.VRMRetentionRetainService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

final class Payment @Inject()(vrmRetentionRetainService: VRMRetentionRetainService,
                              paymentSolveService: PaymentSolveService,
                              dateService: DateService)
                             (implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {

  def begin = Action.async {
    implicit request =>
      (request.cookies.getString(TransactionIdCacheKey), request.cookies.getModel[VehicleAndKeeperLookupFormModel]) match {
        case (Some(transactionId), Some(vehiclesLookupForm)) =>
          callBeginWebPaymentService(transactionId, vehiclesLookupForm.registrationNumber)
        case _ => Future.successful {
          Redirect(routes.MicroServiceError.present()) // TODO is this the correct redirect?
        }
      }
  }

  // The token is checked in the common project, we do nothing with it here.
  def callback(token: String) = Action {
    implicit request =>
      Ok(views.html.vrm_retention.payment_callback_interstitial())
  }

  def getWebPayment = Action.async {
    implicit request =>
      (request.cookies.getString(TransactionIdCacheKey), request.cookies.getString(PaymentTransactionReferenceCacheKey)) match {
        case (Some(transactionId), Some(trxRef)) =>
          callGetWebPaymentService(transactionId, trxRef)
        case _ => Future.successful {
          Redirect(routes.MicroServiceError.present()) // TODO is this the correct redirect?
        }
      }
  }

  def cancel = Action.async {
    implicit request =>
      (request.cookies.getString(TransactionIdCacheKey), request.cookies.getString(PaymentTransactionReferenceCacheKey)) match {
        case (Some(transactionId), Some(trxRef)) =>
          callCancelWebPaymentService(transactionId, trxRef)
        case _ => Future.successful {
          Redirect(routes.MicroServiceError.present()) // TODO is this the correct redirect?
        }
      }
  }

  def exit = Action {
    implicit request =>
      val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)
      val discardedCookies = RelatedCacheKeys.RetainSet ++ {
        if (!storeBusinessDetails) RelatedCacheKeys.BusinessDetailsSet else Set.empty
      }

      Redirect(routes.MockFeedback.present()).discardingCookies(discardedCookies)
  }

  private def microServiceErrorResult(message: String) = {
    Logger.error(message)
    Redirect(routes.MicroServiceError.present())
  }

  private def callBeginWebPaymentService(transactionId: String, vrm: String)(implicit request: Request[_],
                                                                             token: uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.CsrfPreventionToken): Future[Result] = {
    request.headers.get(REFERER) match {
      case Some(referrer) =>

        def paymentBeginFailure = {
          Logger.debug(s"Payment Solve encountered a problem with request ${LogFormats.anonymize(vrm)}, redirect to PaymentFailure")
          Redirect(routes.PaymentFailure.present())
        }

        val paymentCallback = referrer.split(routes.Confirm.present().url)(0) + routes.Payment.callback(token.value).url
        val paymentSolveBeginRequest = PaymentSolveBeginRequest(
          transNo = removeNonNumeric(transactionId), // TODO find a suitable trans no
          vrm = vrm,
          purchaseAmount = Payment.PaymentAmount,
          paymentCallback = paymentCallback
        )
        val trackingId = request.cookies.trackingId()

        paymentSolveService.invoke(paymentSolveBeginRequest, trackingId).map {
          response =>
            if (response.status == Payment.CardDetailsStatus) {
              // TODO need sad path for when redirectUrl is None
              Ok(views.html.vrm_retention.payment(paymentRedirectUrl = response.redirectUrl.get))
                .withCookie(PaymentTransactionReferenceCacheKey, response.trxRef.get)
                .withCookie(REFERER, routes.Payment.begin().url) // The POST from payment service will not contain a REFERER in the header, so use a cookie.
            } else {
              Logger.error("The begin web request to Solve was not validated.")
              paymentBeginFailure
            }
        }.recover {
          case NonFatal(e) =>
            Logger.error(s"Payment Solve web service call failed. Exception " + e.toString.take(45))
            microServiceErrorResult(message = "Payment Solve web service call failed.")
        }
      case _ => Future.successful(Redirect(routes.MicroServiceError.present()))
    }
  }

  private def callGetWebPaymentService(transactionId: String, trxRef: String)
                                      (implicit request: Request[_]): Future[Result] = {

    def paymentNotAuthorised = {
      Logger.debug(s"Payment not authorised for ${LogFormats.anonymize(trxRef)}, redirect to PaymentNotAuthorised")
      Redirect(routes.PaymentNotAuthorised.present())
    }

    val paymentSolveGetRequest = PaymentSolveGetRequest(
      transNo = removeNonNumeric(transactionId), // TODO find a suitable trans no
      trxRef = trxRef
    )
    val trackingId = request.cookies.trackingId()

    paymentSolveService.invoke(paymentSolveGetRequest, trackingId).map {
      response =>
        // TODO store the auth code and masked pan
        if (response.status == Payment.AuthorisedStatus) {
          Redirect(routes.Retain.retain())//.
//            discardingCookie(REFERER) // Not used again.
        } else {
          Logger.debug("The payment was not authorised.")
          paymentNotAuthorised
        }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Payment Solve web service call failed. Exception " + e.toString.take(245))
        microServiceErrorResult(message = "Payment Solve web service call failed.")
    }
  }

  private def callCancelWebPaymentService(transactionId: String, trxRef: String)
                                         (implicit request: Request[_]): Future[Result] = {

    val paymentSolveCancelRequest = PaymentSolveCancelRequest(
      transNo = removeNonNumeric(transactionId), // TODO find a suitable trans no
      trxRef = trxRef
    )
    val trackingId = request.cookies.trackingId()

    paymentSolveService.invoke(paymentSolveCancelRequest, trackingId).map {
      response =>
        if (response.response == Payment.CancelledStatus) {
          Logger.error("The get web request to Solve was not validated.")
        }
        redirectToMockFeedback
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Payment Solve web service call failed. Exception " + e.toString.take(45))
        redirectToMockFeedback
    }
  }

  private def redirectToMockFeedback(implicit request: Request[_]) = {
    val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)
    val cacheKeys = RelatedCacheKeys.RetainSet ++ {
      if (storeBusinessDetails) Set.empty else RelatedCacheKeys.BusinessDetailsSet
    }
    Redirect(routes.MockFeedback.present()).discardingCookies(cacheKeys)
  }

  private def removeNonNumeric(value: String): String =
    value.replaceAll("[^0-9]", "")
}

object Payment {

  private final val CardDetailsStatus = "CARD_DETAILS"
  private final val AuthorisedStatus = "AUTHORISED"
  private final val CancelledStatus = "CANCELLED"
  private final val PaymentAmount = 8000 // TODO where do we get this from?
}