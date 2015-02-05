package controllers

import audit1._
import com.google.inject.Inject
import models._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import play.api.mvc.{Result, _}
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClearTextClientSideSessionFactory, ClientSideSessionFactory}
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.{VssWebEndUserDto, VssWebHeaderDto}
import utils.helpers.Config
import views.vrm_retention.Confirm.ConfirmCacheKey
import views.vrm_retention.Retain._
import views.vrm_retention.VehicleLookup._
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest
import webserviceclients.vrmretentionretain.{VRMRetentionRetainRequest, VRMRetentionRetainService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

final class Retain @Inject()(
                              vrmRetentionRetainService: VRMRetentionRetainService,
                              dateService: DateService,
                              auditService1: audit1.AuditService,
                              auditService2: audit2.AuditService
                              )
                            (implicit clientSideSessionFactory: ClientSideSessionFactory,

                             config2: Config) extends Controller {

  def retain = Action.async { implicit request =>
    (request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[PaymentModel]) match {
      case (Some(vehiclesLookupForm), Some(transactionId), Some(paymentModel)) =>
        retainVrm(vehiclesLookupForm, transactionId, paymentModel.trxRef.get)
      case (_, Some(transactionId), _) => {
        auditService1.send(AuditMessage.from(
          pageMovement = AuditMessage.PaymentToMicroServiceError,
          transactionId = transactionId,
          timestamp = dateService.dateTimeISOChronology
        ))
        auditService2.send(AuditRequest.from(
          pageMovement = AuditMessage.PaymentToMicroServiceError,
          transactionId = transactionId,
          timestamp = dateService.dateTimeISOChronology
        ))
        Future.successful {
          Redirect(routes.MicroServiceError.present())
        }
      }
      case _ =>
        Future.successful {
          Redirect(routes.Error.present("user went to Retain retainMark without correct cookies"))
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

      var paymentModel = request.cookies.getModel[PaymentModel].get
      paymentModel.paymentStatus = Some(Payment.SettledStatus)

      auditService1.send(AuditMessage.from(
        pageMovement = AuditMessage.PaymentToSuccess,
        transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
        keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
        businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
        paymentModel = Some(paymentModel),
        retentionCertId = Some(certificateNumber)))
      auditService2.send(AuditRequest.from(
        pageMovement = AuditMessage.PaymentToSuccess,
        transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
        keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
        businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
        paymentModel = Some(paymentModel),
        retentionCertId = Some(certificateNumber)))

      Redirect(routes.SuccessPayment.present()).
        withCookie(paymentModel).
        withCookie(RetainModel.from(certificateNumber, transactionTimestampWithZone))
    }

    def retainFailure(responseCode: String) = {
      Logger.debug(s"VRMRetentionRetain encountered a problem with request" +
        s" ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.referenceNumber)}" +
        s" ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.registrationNumber)}," +
        s" redirect to VehicleLookupFailure")
      var paymentModel = request.cookies.getModel[PaymentModel].get
      paymentModel.paymentStatus = Some(Payment.CancelledStatus)

      auditService1.send(AuditMessage.from(
        pageMovement = AuditMessage.PaymentToPaymentFailure,
        transactionId = request.cookies.getString(TransactionIdCacheKey).get,
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
        keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
        businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
        paymentModel = Some(paymentModel),
        rejectionCode = Some(responseCode)))
      auditService2.send(AuditRequest.from(
        pageMovement = AuditMessage.PaymentToPaymentFailure,
        transactionId = request.cookies.getString(TransactionIdCacheKey).get,
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        replacementVrm = Some(request.cookies.getModel[EligibilityModel].get.replacementVRM),
        keeperEmail = request.cookies.getModel[ConfirmFormModel].flatMap(_.keeperEmail),
        businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
        paymentModel = Some(paymentModel),
        rejectionCode = Some(responseCode)))

      Redirect(routes.RetainFailure.present()).
        withCookie(paymentModel).
        withCookie(key = RetainResponseCodeCacheKey, value = responseCode.split(" - ")(1))
    }

    def microServiceErrorResult(message: String) = {
      Logger.error(message)
      Redirect(routes.MicroServiceError.present())
    }

    val trackingId = request.cookies.trackingId()

    val vrmRetentionRetainRequest = VRMRetentionRetainRequest(
      webHeader = buildWebHeader(trackingId),
      currentVRM = vehicleAndKeeperLookupFormModel.registrationNumber,
      transactionTimestamp = dateService.today.toDateTimeMillis.get
    )

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

  private def buildWebHeader(trackingId: String): VssWebHeaderDto = {
    VssWebHeaderDto(transactionId = trackingId,
      originDateTime = new DateTime,
      applicationCode = config2.applicationCode,
      serviceTypeCode = config2.vssServiceTypeCode,
      buildEndUser())
  }

  private def buildEndUser(): VssWebEndUserDto = {
    VssWebEndUserDto(endUserId = config2.orgBusinessUnit, orgBusUnit = config2.orgBusinessUnit)
  }
}