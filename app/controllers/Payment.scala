package controllers

import com.google.inject.Inject
import models.BusinessDetailsModel
import models.CacheKeyPrefix
import models.ConfirmFormModel
import models.EligibilityModel
import models.PaymentModel
import models.RetainModel
import models.VehicleAndKeeperLookupFormModel
import org.apache.commons.codec.binary.Base64
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import play.api.Logger
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Request
import play.api.mvc.Result
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_retention.Payment.PaymentTransNoCacheKey
import views.vrm_retention.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_retention.VehicleLookup.TransactionIdCacheKey
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest
import webserviceclients.paymentsolve.RefererFromHeader
import webserviceclients.paymentsolve.PaymentSolveBeginRequest
import webserviceclients.paymentsolve.PaymentSolveCancelRequest
import webserviceclients.paymentsolve.PaymentSolveGetRequest
import webserviceclients.paymentsolve.PaymentSolveService

final class Payment @Inject()(
                               paymentSolveService: PaymentSolveService,
                               refererFromHeader: RefererFromHeader,
                               auditService2: audit2.AuditService
                               )
                             (implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config,
                              dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService) extends Controller {

  def begin = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getModel[EligibilityModel],
      request.cookies.getModel[RetainModel],
      request.cookies.getModel[ConfirmFormModel]) match {
      case (Some(transactionId), Some(vehiclesLookupForm), Some(eligibilityModel), None, Some(confirmFormModel)) =>
        callBeginWebPaymentService(transactionId, vehiclesLookupForm.registrationNumber)
      case _ => Future.successful {
        Redirect(routes.Confirm.present())
      }
    }
  }

  // The token is checked in the common project, we do nothing with it here.
  def callback(token: String) = Action.async { implicit request =>
    // check whether it is past the closing time
    if (new DateTime(dateService.now, DateTimeZone.forID("Europe/London")).getHourOfDay >= config.closing)
      (request.cookies.getString(TransactionIdCacheKey), request.cookies.getModel[PaymentModel]) match {
        case (Some(transactionId), Some(paymentDetails)) =>
          callCancelWebPaymentService(transactionId, paymentDetails.trxRef.get, paymentDetails.isPrimaryUrl).map { _ =>
            Redirect(routes.PaymentPostShutdown.present())
          }
        case _ =>
          Future.successful(Redirect(routes.PaymentPostShutdown.present()))
      }
    else
      Future.successful(Redirect(routes.Payment.getWebPayment()))
  }

  def getWebPayment = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey), request.cookies.getModel[PaymentModel]) match {
      case (Some(transactionId), Some(paymentDetails)) =>
        callGetWebPaymentService(transactionId, paymentDetails.trxRef.get, paymentDetails.isPrimaryUrl)
      case _ => Future.successful {
        paymentFailure("Payment getWebPayment missing TransactionIdCacheKey or PaymentTransactionReferenceCacheKey cookie")
      }
    }
  }

  def cancel = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey), request.cookies.getModel[PaymentModel]) match {
      case (Some(transactionId), Some(paymentDetails)) =>

        auditService2.send(AuditRequest.from(
          pageMovement = AuditRequest.PaymentToExit,
          transactionId = transactionId,
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
          replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
          keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
          businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))

        Future.successful {
          redirectToLeaveFeedback
        }

      case _ => Future.successful {
        paymentFailure("Payment cancel missing TransactionIdCacheKey or PaymentTransactionReferenceCacheKey cookie")
      }
    }
  }

  private def paymentFailure(message: String)(implicit request: Request[_]) = {
    Logger.error(message)

    auditService2.send(AuditRequest.from(
      pageMovement = AuditRequest.PaymentToPaymentFailure,
      transactionId = request.cookies.getString(TransactionIdCacheKey)
        .getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
      timestamp = dateService.dateTimeISOChronology,
      vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
      replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
      keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
      businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
      paymentModel = request.cookies.getModel[PaymentModel],
      rejectionCode = Some(message)))

    Redirect(routes.PaymentFailure.present())
  }

  private def callBeginWebPaymentService(transactionId: String, vrm: String)
                                        (implicit request: Request[_],
                                         token: uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.CsrfPreventionToken): Future[Result] = {
    refererFromHeader.fetch match {
      case Some(referer) =>
        val tokenBase64URLSafe = Base64.encodeBase64URLSafeString(token.value.getBytes)
        val paymentCallback = refererFromHeader.paymentCallbackUrl(referer = referer, tokenBase64URLSafe = tokenBase64URLSafe)
        val transNo = request.cookies.getString(PaymentTransNoCacheKey).get
        val paymentSolveBeginRequest = PaymentSolveBeginRequest(
          transactionId = transactionId,
          transNo = transNo,
          vrm = vrm,
          purchaseAmount = config.purchaseAmount.toInt,
          paymentCallback = paymentCallback
        )
        val trackingId = request.cookies.trackingId()

        paymentSolveService.invoke(paymentSolveBeginRequest, trackingId).map { response =>
          if (response.status == Payment.CardDetailsStatus) {
            Ok(views.html.vrm_retention.payment(paymentRedirectUrl = response.redirectUrl.get))
              .withCookie(PaymentModel.from(trxRef = response.trxRef.get, isPrimaryUrl = response.isPrimaryUrl))
              .withCookie(REFERER, routes.Payment.begin().url) // The POST from payment service will not contain a REFERER in the header, so use a cookie.
          } else {
            val msg = s"The begin web request to Solve was not validated. Payment Solve encountered a problem " +
              s"with request ${LogFormats.anonymize(vrm)}, redirect to PaymentFailure"
            paymentFailure(msg)
          }
        }.recover {
          case NonFatal(e) =>
            paymentFailure(message = s"Payment Solve web service call with paymentSolveBeginRequest failed. Exception " + e.toString)
        }
      case _ => Future.successful(paymentFailure(message = "Payment callBeginWebPaymentService no referer"))
    }
  }

  private def callGetWebPaymentService(transactionId: String, trxRef: String, isPrimaryUrl: Boolean)
                                      (implicit request: Request[_]): Future[Result] = {

    def paymentNotAuthorised = {
      Logger.debug(s"Payment not authorised for ${LogFormats.anonymize(trxRef)}, redirect to PaymentNotAuthorised")

      val paymentModel = request.cookies.getModel[PaymentModel].get

      auditService2.send(AuditRequest.from(
        pageMovement = AuditRequest.PaymentToPaymentNotAuthorised,
        transactionId = request.cookies.getString(TransactionIdCacheKey)
          .getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
        keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
        businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
        paymentModel = Some(paymentModel)))

      Redirect(routes.PaymentNotAuthorised.present())
        .withCookie(paymentModel)
    }

    val transNo = request.cookies.getString(PaymentTransNoCacheKey).get

    val paymentSolveGetRequest = PaymentSolveGetRequest(
      transNo = transNo,
      trxRef = trxRef,
      isPrimaryUrl = isPrimaryUrl
    )
    val trackingId = request.cookies.trackingId()

    paymentSolveService.invoke(paymentSolveGetRequest, trackingId).map { response =>
      if (response.status == Payment.AuthorisedStatus) {

        val paymentModel = request.cookies.getModel[PaymentModel].get

        paymentModel.authCode = response.authcode
        paymentModel.maskedPAN = response.maskedPAN
        paymentModel.cardType = response.cardType
        paymentModel.merchantId = response.merchantTransactionId
        paymentModel.paymentType = response.paymentType
        paymentModel.totalAmountPaid = response.purchaseAmount
        paymentModel.paymentStatus = Some(Payment.AuthorisedStatus)

        Redirect(routes.Retain.retain())
          .discardingCookie(REFERER) // Not used again.
          .withCookie(paymentModel)
      } else {
        Logger.debug("The payment was not authorised.")
        paymentNotAuthorised
      }
    }.recover {
      case NonFatal(e) =>
        paymentFailure(message = "Payment Solve web service call with paymentSolveGetRequest failed: " + e.toString)
    }
  }

  private def callCancelWebPaymentService(transactionId: String, trxRef: String, isPrimaryUrl: Boolean)
                                         (implicit request: Request[_]): Future[Result] = {

    val transNo = request.cookies.getString(PaymentTransNoCacheKey).get

    val paymentSolveCancelRequest = PaymentSolveCancelRequest(
      transNo = transNo,
      trxRef = trxRef,
      isPrimaryUrl = isPrimaryUrl
    )
    val trackingId = request.cookies.trackingId()

    paymentSolveService.invoke(paymentSolveCancelRequest, trackingId).map { response =>
      if (response.response == Payment.CancelledStatus) {
        Logger.error("The get web request to Solve was not validated.")
      }

      auditService2.send(AuditRequest.from(
        pageMovement = AuditRequest.PaymentToExit,
        transactionId = request.cookies.getString(TransactionIdCacheKey)
          .getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
        keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
        businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))

      redirectToLeaveFeedback
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Payment Solve web service call with paymentSolveCancelRequest failed. Exception " + e.toString)
        redirectToLeaveFeedback
    }
  }

  private def redirectToLeaveFeedback(implicit request: Request[_]) = {
    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }
}

object Payment {

  final val CardDetailsStatus = "CARD_DETAILS"
  final val AuthorisedStatus = "AUTHORISED"
  final val CancelledStatus = "CANCELLED"
  final val SettledStatus = "SETTLED"
}