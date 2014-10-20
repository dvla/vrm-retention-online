package controllers

import com.google.inject.Inject
import composition.RefererFromHeader
import models.VehicleAndKeeperLookupFormModel
import org.apache.commons.codec.binary.Base64
import play.api.Logger
import play.api.mvc.{Action, Controller, Request, Result}
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import views.vrm_retention.ConfirmBusiness._
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
                              dateService: DateService,
                              refererFromHeader: RefererFromHeader)
                             (implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {

  def begin = Action.async {
    implicit request =>
      (request.cookies.getString(TransactionIdCacheKey), request.cookies.getModel[VehicleAndKeeperLookupFormModel]) match {
        case (Some(transactionId), Some(vehiclesLookupForm)) =>
          callBeginWebPaymentService(transactionId, vehiclesLookupForm.registrationNumber)
        case _ => Future.successful {
          paymentFailure("Payment begin missing TransactionIdCacheKey or VehicleAndKeeperLookupFormModel cookie") // TODO is this the correct redirect?
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
          paymentFailure("Payment getWebPayment missing TransactionIdCacheKey or PaymentTransactionReferenceCacheKey cookie") // TODO is this the correct redirect?
        }
      }
  }

  def cancel = Action.async {
    implicit request =>
      (request.cookies.getString(TransactionIdCacheKey), request.cookies.getString(PaymentTransactionReferenceCacheKey)) match {
        case (Some(transactionId), Some(trxRef)) =>
          callCancelWebPaymentService(transactionId, trxRef)
        case _ => Future.successful {
          paymentFailure("Payment cancel missing TransactionIdCacheKey or PaymentTransactionReferenceCacheKey cookie") // TODO is this the correct redirect?
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

  private def paymentFailure(message: String) = {
    Logger.error(message)
    //    Redirect(routes.MicroServiceError.present())
    Redirect(routes.PaymentFailure.present())
  }

  private def callBeginWebPaymentService(transactionId: String, vrm: String)(implicit request: Request[_],
                                                                             token: uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.CsrfPreventionToken): Future[Result] = {
    refererFromHeader.fetch match {
      case Some(referrer) =>
        val tokenBase64URLSafe = Base64.encodeBase64URLSafeString(token.value.getBytes)
        val paymentCallback = referrer.split(routes.Confirm.present().url)(0) + routes.Payment.callback(tokenBase64URLSafe).url
        val paymentSolveBeginRequest = PaymentSolveBeginRequest(
          transNo = removeNonNumeric(transactionId), // TODO find a suitable trans no
          vrm = vrm,
          purchaseAmount = config.purchaseAmount.toInt,
          paymentCallback = paymentCallback
        )
        val trackingId = request.cookies.trackingId()

        paymentSolveService.invoke(paymentSolveBeginRequest, trackingId).map {
          response =>
            if (response.status == Payment.CardDetailsStatus) {
              // TODO need sad path for when redirectUrl is None
//              Ok(views.html.vrm_retention.payment(paymentRedirectUrl = response.redirectUrl.get))
              Redirect(response.redirectUrl.get)
                .withCookie(PaymentTransactionReferenceCacheKey, response.trxRef.get)
                .withCookie(REFERER, routes.Payment.begin().url) // The POST from payment service will not contain a REFERER in the header, so use a cookie.
            } else {
              paymentFailure(s"The begin web request to Solve was not validated. Payment Solve encountered a problem with request ${LogFormats.anonymize(vrm)}, redirect to PaymentFailure")
            }
        }.recover {
          case NonFatal(e) =>
            paymentFailure(message = s"Payment Solve web service call with paymentSolveBeginRequest failed. Exception " + e.toString)
        }
      case _ => Future.successful(paymentFailure(message = "Payment callBeginWebPaymentService no referer"))
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
          Redirect(routes.Retain.retain()).
            discardingCookie(REFERER) // Not used again.
        } else {
          Logger.debug("The payment was not authorised.")
          paymentNotAuthorised
        }
    }.recover {
      case NonFatal(e) =>
        paymentFailure(message = "Payment Solve web service call with paymentSolveGetRequest failed: " + e.toString)
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
        Logger.error(s"Payment Solve web service call with paymentSolveCancelRequest failed. Exception " + e.toString)
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
}