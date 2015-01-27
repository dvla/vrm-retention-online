package controllers

import audit1.AuditMessage
import com.google.inject.Inject
import mappings.common.ErrorCodes
import models._
import org.joda.time.format.ISODateTimeFormat
import play.api.data.{Form => PlayForm, FormError}
import play.api.mvc.{Call, _}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClearTextClientSideSessionFactory, ClientSideSessionFactory}
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupBase
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupBase.{LookupResult, VehicleFound, VehicleNotFound}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.constraints.Postcode.formatPostcode
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.DmsWebHeaderDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsRequest
import utils.helpers.{Config, Config2}
import views.vrm_retention.Payment._
import views.vrm_retention.RelatedCacheKeys
import views.vrm_retention.VehicleLookup._
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest
import webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class VehicleLookup @Inject()(
                                     val bruteForceService: BruteForcePreventionService,
                                     vehicleAndKeeperLookupService: VehicleAndKeeperLookupService,
                                     dateService: DateService,
                                     auditService1: audit1.AuditService,
                                     auditService2: audit2.AuditService
                                     )
                                   (implicit val clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config,
                                    config2: Config2) extends VehicleLookupBase {

  override val vrmLocked: Call = routes.VrmLocked.present()
  override val microServiceError: Call = routes.MicroServiceError.present()
  override val vehicleLookupFailure: Call = routes.VehicleLookupFailure.present()
  override val responseCodeCacheKey: String = VehicleAndKeeperLookupResponseCodeCacheKey

  override type Form = VehicleAndKeeperLookupFormModel

  private[controllers] val form = PlayForm(
    VehicleAndKeeperLookupFormModel.Form.Mapping
  )

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.vehicle_lookup(form.fill())).
      discardingCookies(RelatedCacheKeys.VehicleAndKeeperLookupSet)
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => Future.successful {
        BadRequest(views.html.vrm_retention.vehicle_lookup(formWithReplacedErrors(invalidForm)))
      },
      validForm => {
        bruteForceAndLookup(
          validForm.registrationNumber,
          validForm.referenceNumber,
          validForm)
          .map(_.withCookie(TransactionIdCacheKey, transactionId(validForm)))
          .map(_.withCookie(PaymentTransNoCacheKey, calculatePaymentTransNo))
      }
    )
  }

  def back = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present())
  }

  override protected def callLookupService(trackingId: String, form: Form)(implicit request: Request[_]): Future[LookupResult] = {
    val vehicleAndKeeperDetailsRequest = VehicleAndKeeperDetailsRequest(
      dmsHeader = buildHeader(trackingId),
      referenceNumber = form.referenceNumber,
      registrationNumber = form.registrationNumber,
      transactionTimestamp = dateService.now.toDateTime
    )
    vehicleAndKeeperLookupService.invoke(vehicleAndKeeperDetailsRequest, trackingId).map { response =>
      response.responseCode match {
        case Some(responseCode) =>
          auditService1.send(AuditMessage.from(
            pageMovement = AuditMessage.VehicleLookupToVehicleLookupFailure,
            transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
            timestamp = dateService.dateTimeISOChronology,
            vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
            rejectionCode = Some(responseCode)))
          auditService2.send(AuditRequest.from(
            pageMovement = AuditMessage.VehicleLookupToVehicleLookupFailure,
            transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
            timestamp = dateService.dateTimeISOChronology,
            vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
            rejectionCode = Some(responseCode)))

          VehicleNotFound(responseCode.split(" - ")(1))

        case None =>
          response.vehicleAndKeeperDetailsDto match {
            case Some(dto) if !formatPostcode(form.postcode).equals(formatPostcode(dto.keeperPostcode.get)) =>
              auditService1.send(AuditMessage.from(
                pageMovement = AuditMessage.VehicleLookupToVehicleLookupFailure,
                transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
                timestamp = dateService.dateTimeISOChronology,
                vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
                rejectionCode = Some(ErrorCodes.PostcodeMismatchErrorCode + " - vehicle_and_keeper_lookup_keeper_postcode_mismatch")))
              auditService2.send(AuditRequest.from(
                pageMovement = AuditMessage.VehicleLookupToVehicleLookupFailure,
                transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
                timestamp = dateService.dateTimeISOChronology,
                vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
                rejectionCode = Some(ErrorCodes.PostcodeMismatchErrorCode + " - vehicle_and_keeper_lookup_keeper_postcode_mismatch")))

              VehicleNotFound("vehicle_and_keeper_lookup_keeper_postcode_mismatch")

            case Some(dto) =>
              VehicleFound(Redirect(routes.CheckEligibility.present()).
                withCookie(VehicleAndKeeperDetailsModel.from(dto)))

            case None =>
              throw new RuntimeException("No Dto in vehicleAndKeeperLookupService response")
          }
      }
    }
  }

  private def transactionId(validForm: VehicleAndKeeperLookupFormModel): String = {
    val transactionTimestamp = dateService.today.toDateTimeMillis.get
    val isoDateTimeString = ISODateTimeFormat.yearMonthDay().print(transactionTimestamp).drop(2) + " " +
      ISODateTimeFormat.hourMinuteSecond().print(transactionTimestamp)
    validForm.registrationNumber +
      isoDateTimeString.replace(" ", "").replace("-", "").replace(":", "").replace(".", "")
  }

  private def formWithReplacedErrors(form: PlayForm[VehicleAndKeeperLookupFormModel])(implicit request: Request[_]) =
    (form /: List(
      (VehicleRegistrationNumberId, "error.restricted.validVrnOnly"),
      (DocumentReferenceNumberId, "error.validDocumentReferenceNumber"),
      (PostcodeId, "error.restricted.validPostcode"))) { (form, error) =>
      form.replaceError(error._1, FormError(
        key = error._1,
        message = error._2,
        args = Seq.empty
      ))
    }.distinctErrors

  // payment solve requires (for each day) a unique six digit number
  // use time from midnight in tenths of a second units
  private def calculatePaymentTransNo = {
    val milliSecondsFromMidnight = dateService.today.toDateTime.get.millisOfDay().get()
    val tenthSecondsFromMidnight = (milliSecondsFromMidnight / 100.0).toInt
    // prepend with zeros
    "%06d".format(tenthSecondsFromMidnight)
  }

  private def buildHeader(trackingId: String): DmsWebHeaderDto = {
    val alwaysLog = true
    val englishLanguage = "EN"
    DmsWebHeaderDto(conversationId = trackingId,
      originDateTime = dateService.now.toDateTime,
      applicationCode = config.applicationCode,
      channelCode = config.channelCode,
      contactId = config.contactId,
      eventFlag = alwaysLog,
      serviceTypeCode = config.serviceTypeCode,
      languageCode = englishLanguage,
      endUser = None)
  }
}
