package controllers

import com.google.inject.Inject
import models.{VehicleAndKeeperDetailsModel, RetainModel, VehicleAndKeeperLookupFormModel}
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import play.api.mvc.{Result, _}
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import views.vrm_retention.Payment._
import views.vrm_retention.Retain._
import views.vrm_retention.VehicleLookup._
import webserviceclients.vrmretentionretain.{VRMRetentionRetainRequest, VRMRetentionRetainService}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import views.vrm_retention.CheckEligibility._
import views.vrm_retention.Confirm.KeeperEmailCacheKey
import scala.Some
import play.api.mvc.Result
import audit.{PaymentToSuccessAuditMessage, AuditService, ConfirmToPaymentAuditMessage}

final class Retain @Inject()(vrmRetentionRetainService: VRMRetentionRetainService,
                             dateService: DateService,
                             auditService: AuditService)
                            (implicit clientSideSessionFactory: ClientSideSessionFactory,
                             config: Config) extends Controller {

  def retain = Action.async {
    implicit request =>
      (request.cookies.getModel[VehicleAndKeeperLookupFormModel],
        request.cookies.getString(TransactionIdCacheKey),
        request.cookies.getString(PaymentTransactionReferenceCacheKey)) match {
        case (Some(vehiclesLookupForm), Some(transactionId), Some(trxRef)) => retainVrm(vehiclesLookupForm, transactionId, trxRef)
        case _ => Future.successful {
          Redirect(routes.MicroServiceError.present()) // TODO need an error page for this scenario
        }
      }
  }

  private def retainVrm(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                        transactionId: String, trxRef: String)
                       (implicit request: Request[_]): Future[Result] = {

    def retainSuccess(certificateNumber: String) = {

      // create the transaction timestamp
      val transactionTimestamp = dateService.today.toDateTimeMillis.get
      val isoDateTimeString = ISODateTimeFormat.yearMonthDay().print(transactionTimestamp) + " " +
        ISODateTimeFormat.hourMinuteSecondMillis().print(transactionTimestamp)
      val transactionTimestampWithZone = s"$isoDateTimeString:${transactionTimestamp.getZone}"

      // retrieve audit values not already in scope
      val vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel].get
      val transactionId = request.cookies.getString(TransactionIdCacheKey).get
      val replacementVRM = request.cookies.getString(CheckEligibilityCacheKey).get
      val keeperEmail = request.cookies.getString(KeeperEmailCacheKey)

      auditService.send(PaymentToSuccessAuditMessage.from(
        vehicleAndKeeperLookupFormModel, vehicleAndKeeperDetailsModel, transactionId, vehicleAndKeeperDetailsModel.registrationNumber,
        replacementVRM, keeperEmail))

      Redirect(routes.SuccessPayment.present()).
        withCookie(RetainModel.from(certificateNumber, transactionTimestampWithZone))
    }

    def retainFailure(responseCode: String) = {
      Logger.debug(s"VRMRetentionRetain encountered a problem with request" +
        s" ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.referenceNumber)}" +
        s" ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.registrationNumber)}," +
        s" redirect to VehicleLookupFailure")
      Redirect(routes.RetainFailure.present()).
        withCookie(key = RetainResponseCodeCacheKey, value = responseCode)
    }

    def microServiceErrorResult(message: String) = {
      Logger.error(message)
      Redirect(routes.MicroServiceError.present())
    }

    val vrmRetentionRetainRequest = VRMRetentionRetainRequest(
      currentVRM = vehicleAndKeeperLookupFormModel.registrationNumber,
      transactionTimestamp = dateService.today.toDateTimeMillis.get
    )
    val trackingId = request.cookies.trackingId()

    vrmRetentionRetainService.invoke(vrmRetentionRetainRequest, trackingId).map {
      response =>
        response.responseCode match {
          case Some(responseCode) => retainFailure(responseCode) // There is only a response code when there is a problem.
          case None =>
            // Happy path when there is no response code therefore no problem.
            response.certificateNumber match {
              case Some(certificateNumber) => retainSuccess(certificateNumber)
              case _ => microServiceErrorResult(message = "Certificate number not found in response")
            }
        }
    }.recover {
      case NonFatal(e) =>
        microServiceErrorResult(s"VRM Retention Retain web service call failed. Exception " + e.toString)
    }
  }
}