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
import play.api.mvc.{Action, Controller, Request, Result}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClearTextClientSideSessionFactory
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichCookies
import common.clientsidesession.CookieImplicits.RichResult
import common.filters.CsrfPreventionAction.CsrfPreventionToken
import common.LogFormats.anonymize
import common.LogFormats.DVLALogger
import common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_retention.Payment.PaymentTransNoCacheKey
import views.vrm_retention.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_retention.VehicleLookup.TransactionIdCacheKey
import webserviceclients.audit2.AuditRequest
import webserviceclients.paymentsolve.RefererFromHeader
import webserviceclients.paymentsolve.PaymentSolveBeginRequest
import webserviceclients.paymentsolve.PaymentSolveCancelRequest
import webserviceclients.paymentsolve.PaymentSolveGetRequest
import webserviceclients.paymentsolve.PaymentSolveService

final class Payment @Inject()(paymentSolveService: PaymentSolveService,
                               refererFromHeader: RefererFromHeader,
                               auditService2: webserviceclients.audit2.AuditService)
                             (implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config,
                              dateService: common.services.DateService) extends Controller with DVLALogger {

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
    if (new DateTime(dateService.now, DateTimeZone.forID("Europe/London")).getMinuteOfDay >= config.closingTimeMinOfDay)
      (request.cookies.getString(TransactionIdCacheKey), request.cookies.getModel[PaymentModel]) match {
        case (Some(transactionId), Some(paymentDetails)) =>
          callCancelWebPaymentService(transactionId, paymentDetails.trxRef.get, paymentDetails.isPrimaryUrl).map { _ =>
            Redirect(routes.PaymentPostShutdown.present())
          }
        case _ =>
          Future.successful(Redirect(routes.PaymentPostShutdown.present()))
      }
    else {
      val msg = "Callback method on Payment controller invoked. " +
        "We have now returned from Logic Group payment pages and will now call getWebPayment on the Payment ms " +
        "to check if the payment was authorised..."
      logMessage(request.cookies.trackingId(), Info, msg)
      Future.successful(Redirect(routes.Payment.getWebPayment()))
    }
  }

  def getWebPayment = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey), request.cookies.getModel[PaymentModel]) match {
      case (Some(transactionId), Some(paymentDetails)) =>
        callGetWebPaymentService(transactionId, paymentDetails.trxRef.get, paymentDetails.isPrimaryUrl)
      case _ => Future.successful {
        paymentFailure(
          "Payment getWebPayment missing TransactionIdCacheKey or PaymentTransactionReferenceCacheKey cookie"
        )
      }
    }
  }

  def cancel = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey), request.cookies.getModel[PaymentModel]) match {
      case (Some(transactionId), Some(paymentDetails)) =>
        val trackingId = request.cookies.trackingId()
        auditService2.send(
          AuditRequest.from(
            trackingId = trackingId,
            pageMovement = AuditRequest.PaymentToExit,
            transactionId = transactionId,
            timestamp = dateService.dateTimeISOChronology,
            documentReferenceNumber = request.cookies.getModel[VehicleAndKeeperLookupFormModel].map(_.referenceNumber),
            vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
            replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
            keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
            businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]
          ), trackingId
        )

        Future.successful {
          redirectToLeaveFeedback
        }
      case _ => Future.successful {
        paymentFailure("Payment cancel missing TransactionIdCacheKey or PaymentTransactionReferenceCacheKey cookie")
      }
    }
  }

  private def paymentFailure(message: String)(implicit request: Request[_]) = {
    logMessage(request.cookies.trackingId(),Error, message)
    val trackingId = request.cookies.trackingId()
    auditService2.send(
      AuditRequest.from(
        trackingId = trackingId,
        pageMovement = AuditRequest.PaymentToPaymentFailure,
        transactionId = request.cookies.getString(TransactionIdCacheKey)
          .getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId.value),
        timestamp = dateService.dateTimeISOChronology,
        documentReferenceNumber = request.cookies.getModel[VehicleAndKeeperLookupFormModel].map(_.referenceNumber),
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
        keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
        businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
        paymentModel = request.cookies.getModel[PaymentModel],
        rejectionCode = Some(message)
      ), trackingId
    )

    Redirect(routes.PaymentFailure.present())
  }

  private def callBeginWebPaymentService(transactionId: String, vrm: String)
                                        (implicit request: Request[_],
                                         token: CsrfPreventionToken): Future[Result] = {
    refererFromHeader.fetch match {
      case Some(referer) =>
        val tokenBase64URLSafe = Base64.encodeBase64URLSafeString(token.value.getBytes)
        val paymentCallback = refererFromHeader.paymentCallbackUrl(
          referer = referer,
          tokenBase64URLSafe = tokenBase64URLSafe
        )
        val transNo = request.cookies.getString(PaymentTransNoCacheKey).get
        val paymentSolveBeginRequest = PaymentSolveBeginRequest(
          transactionId = transactionId,
          transNo = transNo,
          vrm = vrm,
          purchaseAmount = config.purchaseAmountInPence.toInt,
          paymentCallback = paymentCallback
        )
        val trackingId = request.cookies.trackingId()

        paymentSolveService.invoke(paymentSolveBeginRequest, trackingId).map {
          case (OK, response) if response.beginResponse.status == Payment.CardDetailsStatus =>
            val msg = "Presenting payment view with embedded iframe source set to the following redirectUrl " +
              s"from Logic Group: ${response.redirectUrl.get}. We are now entering Logic Group payment pages."
            logMessage(request.cookies.trackingId(), Info, msg)
            Ok(views.html.vrm_retention.payment(paymentRedirectUrl = response.redirectUrl.get))
              .withCookie(PaymentModel.from(trxRef = response.trxRef.get, isPrimaryUrl = response.isPrimaryUrl))
              // The POST from payment service will not contain a REFERER in the header, so use a cookie.
              .withCookie(REFERER, routes.Payment.begin().url)
          case (_, response) =>
            paymentFailure(s"The begin web request to Solve encountered a problem with request " +
              s"${anonymize(vrm)}, response: ${response.beginResponse.response}, " +
              s"status: ${response.beginResponse.status}, redirect to PaymentFailure")
        }.recover {
          case NonFatal(e) =>
            paymentFailure(
              message = s"Payment Solve web service call with paymentSolveBeginRequest failed. Exception " + e.toString
            )
        }
      case _ => Future.successful(paymentFailure(message = "Payment callBeginWebPaymentService no referer"))
    }
  }

  private def callGetWebPaymentService(transactionId: String, trxRef: String, isPrimaryUrl: Boolean)
                                      (implicit request: Request[_]): Future[Result] = {

    def paymentNotAuthorised = {
      val msg = s"Payment not authorised for ${anonymize(trxRef)}, redirecting to PaymentNotAuthorised"
      logMessage(request.cookies.trackingId(), Debug, msg)

      val paymentModel = request.cookies.getModel[PaymentModel].get

      val trackingId = request.cookies.trackingId()
      auditService2.send(
        AuditRequest.from(
          trackingId = trackingId,
          pageMovement = AuditRequest.PaymentToPaymentNotAuthorised,
          transactionId = request.cookies.getString(TransactionIdCacheKey)
            .getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId.value),
          timestamp = dateService.dateTimeISOChronology,
          documentReferenceNumber = request.cookies.getModel[VehicleAndKeeperLookupFormModel].map(_.referenceNumber),
          vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
          replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
          keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
          businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
          paymentModel = Some(paymentModel)
        ), trackingId
      )

      Redirect(routes.PaymentNotAuthorised.present()).withCookie(paymentModel)
    }

    val transNo = request.cookies.getString(PaymentTransNoCacheKey).get

    val paymentSolveGetRequest = PaymentSolveGetRequest(transNo = transNo, trxRef = trxRef, isPrimaryUrl = isPrimaryUrl)
    val trackingId = request.cookies.trackingId()

    paymentSolveService.invoke(paymentSolveGetRequest, trackingId).map {
      case (OK, response) if response.getResponse.status == Payment.AuthorisedStatus =>
        val paymentModel = request.cookies.getModel[PaymentModel].get

        paymentModel.authCode = response.authcode
        paymentModel.maskedPAN = response.maskedPAN
        paymentModel.cardType = response.cardType
        paymentModel.merchantId = response.merchantTransactionId
        paymentModel.paymentType = response.paymentType
        paymentModel.totalAmountPaid = response.purchaseAmount
        paymentModel.paymentStatus = Some(Payment.AuthorisedStatus)

        val msg = "The payment was successfully authorised, now redirecting to retain - " +
          s"status: ${response.getResponse.status}, response: ${response.getResponse.response}."
      logMessage(request.cookies.trackingId(), Info, msg)

        Redirect(routes.Retain.retain())
          .discardingCookie(REFERER) // Not used again.
          .withCookie(paymentModel)
      case (_, response) =>
        logMessage(request.cookies.trackingId(), Error,
          "The payment was not authorised - " +
          s"status: ${response.getResponse.status}, response: ${response.getResponse.response}.")
        paymentNotAuthorised
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
      if (response._2.status == Payment.CancelledStatus) {
        logMessage(trackingId, Info, "The web request to Solve was cancelled.")
      } else {
        logMessage(trackingId, Error, "The cancel was not successful, " +
          s"response: ${response._2.response}, status: ${response._2.status}.")
      }

      auditService2.send(
        AuditRequest.from(
          trackingId = trackingId,
          pageMovement = AuditRequest.PaymentToExit,
          transactionId = request.cookies.getString(TransactionIdCacheKey)
            .getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId.value),
          timestamp = dateService.dateTimeISOChronology,
          documentReferenceNumber = request.cookies.getModel[VehicleAndKeeperLookupFormModel].map(_.referenceNumber),
          vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
          replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
          keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
          businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]
        ),
        trackingId
      )

      redirectToLeaveFeedback
    }.recover {
      case NonFatal(e) =>
        logMessage(
          trackingId,
          Error,
          "Payment Solve web service call with paymentSolveCancelRequest failed. Exception " + e.toString
        )
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
