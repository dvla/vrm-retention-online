package controllers

import com.google.inject.Inject
import models._
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import play.api.mvc.Result
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebEndUserDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebHeaderDto
import utils.helpers.Config
import views.vrm_retention.Payment._
import views.vrm_retention.Retain._
import views.vrm_retention.VehicleLookup._
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest
import webserviceclients.vrmretentionretain.VRMRetentionRetainRequest
import webserviceclients.vrmretentionretain.VRMRetentionRetainService
import controllers.Payment.AuthorisedStatus

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

final class Retain @Inject()(
                              vrmRetentionRetainService: VRMRetentionRetainService,
                              auditService2: audit2.AuditService
                              )
                            (implicit clientSideSessionFactory: ClientSideSessionFactory,
                             config: Config,
                             dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService) extends Controller {

  def retain = Action.async { implicit request =>
    (request.cookies.getModel[VehicleAndKeeperLookupFormModel],
      request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getString(PaymentTransNoCacheKey),
      request.cookies.getModel[PaymentModel],
      request.cookies.getModel[EligibilityModel]) match {
      case (Some(vehiclesLookupForm), Some(transactionId), Some(paymentTransNo), Some(paymentModel), Some(eligibility)) if paymentModel.paymentStatus == Some(AuthorisedStatus) =>
        retainVrm(vehiclesLookupForm, transactionId, paymentTransNo, paymentModel, eligibility)
      case (_, Some(transactionId), _, _, _) => {
        auditService2.send(AuditRequest.from(
          pageMovement = AuditRequest.PaymentToMicroServiceError,
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
                        transactionId: String, paymentTransNo: String, paymentModel: PaymentModel, eligibility: EligibilityModel)
                       (implicit request: Request[_]): Future[Result] = {

    def retainSuccess(certificateNumber: String) = {

      // create the transaction timestamp
      val transactionTimestamp =
        DayMonthYear.from(new DateTime(dateService.now, DateTimeZone.forID("Europe/London"))).toDateTimeMillis.get
      val isoDateTimeString = ISODateTimeFormat.yearMonthDay().print(transactionTimestamp) + " " +
        ISODateTimeFormat.hourMinuteSecond().print(transactionTimestamp)
      val transactionTimestampWithZone = s"$isoDateTimeString"

      var paymentModel = request.cookies.getModel[PaymentModel].get
      paymentModel.paymentStatus = Some(Payment.SettledStatus)
      val transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId)

      auditService2.send(AuditRequest.from(
        pageMovement = AuditRequest.PaymentToSuccess,
        transactionId = transactionId,
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        replacementVrm = Some(eligibility.replacementVRM),
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

      auditService2.send(AuditRequest.from(
        pageMovement = AuditRequest.PaymentToPaymentFailure,
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
      transactionTimestamp = dateService.today.toDateTimeMillis.get,
      paymentTransNo = paymentTransNo,
      paymentTrxRef = paymentModel.trxRef.get,
      isPaymentPrimaryUrl = paymentModel.isPrimaryUrl)

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
      applicationCode = config.applicationCode,
      serviceTypeCode = config.vssServiceTypeCode,
      buildEndUser())
  }

  private def buildEndUser(): VssWebEndUserDto = {
    VssWebEndUserDto(endUserId = config.orgBusinessUnit, orgBusUnit = config.orgBusinessUnit)
  }
}