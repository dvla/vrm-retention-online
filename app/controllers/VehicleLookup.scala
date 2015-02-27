package controllers

import audit1.AuditMessage
import com.google.inject.Inject
import mappings.common.ErrorCodes
import models._
import org.joda.time.format.ISODateTimeFormat
import play.api.data.{Form => PlayForm, FormError}
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupBase
import uk.gov.dvla.vehicles.presentation.common.model.{BruteForcePreventionModel, VehicleAndKeeperDetailsModel}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.constraints.Postcode.formatPostcode
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber._
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperDetailsDto, VehicleAndKeeperLookupService}
import utils.helpers.Config
import views.vrm_retention.Payment._
import views.vrm_retention.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_retention.VehicleLookup._
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest

import scala.concurrent.Future

final class VehicleLookup @Inject()(implicit bruteForceService: BruteForcePreventionService,
                                    vehicleAndKeeperLookupService: VehicleAndKeeperLookupService,
                                    dateService: DateService,
                                    auditService1: audit1.AuditService,
                                    auditService2: audit2.AuditService,
                                    clientSideSessionFactory: ClientSideSessionFactory,
                                    config2: Config) extends VehicleLookupBase[VehicleAndKeeperLookupFormModel] {

  override val form = PlayForm(
    VehicleAndKeeperLookupFormModel.Form.Mapping
  )
  override val responseCodeCacheKey: String = VehicleAndKeeperLookupResponseCodeCacheKey

  override def vrmLocked(bruteForcePreventionModel: BruteForcePreventionModel, formModel: VehicleAndKeeperLookupFormModel)
                        (implicit request: Request[_]): Result =
    addDefaultCookies(Redirect(routes.VrmLocked.present()),formModel)

  override def microServiceError(t: Throwable, formModel: VehicleAndKeeperLookupFormModel)
                                (implicit request: Request[_]): Result =
    addDefaultCookies(Redirect(routes.MicroServiceError.present()), formModel)

  override def vehicleLookupFailure(responseCode: String, formModel: VehicleAndKeeperLookupFormModel)
                                   (implicit request: Request[_]): Result = {

    val vehicleAndKeeperDetailsModel = new VehicleAndKeeperDetailsModel(
      registrationNumber = formatVrm(formModel.registrationNumber),
      make = None,
      model = None,
      title = None,
      firstName = None,
      lastName = None,
      address = None,
      disposeFlag = None,
      keeperEndDate = None,
      suppressedV5Flag = None
    )

    auditService1.send(AuditMessage.from(
      pageMovement = AuditMessage.VehicleLookupToVehicleLookupFailure,
      transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
      timestamp = dateService.dateTimeISOChronology,
      vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
      rejectionCode = Some(responseCode)))
    auditService2.send(AuditRequest.from(
      pageMovement = AuditMessage.VehicleLookupToVehicleLookupFailure,
      transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
      timestamp = dateService.dateTimeISOChronology,
      vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
      rejectionCode = Some(responseCode)))

    addDefaultCookies(Redirect(routes.VehicleLookupFailure.present()), formModel)
  }

  override def presentResult(implicit request: Request[_]) =
    Ok(views.html.vrm_retention.vehicle_lookup(form)).
      discardingCookies(removeCookiesOnExit)


  override def invalidFormResult(invalidForm: PlayForm[VehicleAndKeeperLookupFormModel])
                                (implicit request: Request[_]): Future[Result] = Future.successful {
    BadRequest(views.html.vrm_retention.vehicle_lookup(formWithReplacedErrors(invalidForm)))
  }

  override def vehicleFoundResult(vehicleAndKeeperDetailsDto: VehicleAndKeeperDetailsDto,
                                  formModel: VehicleAndKeeperLookupFormModel)
                                 (implicit request: Request[_]): Result = {
      if (!formatPostcode(formModel.postcode).equals(formatPostcode(vehicleAndKeeperDetailsDto.keeperPostcode.get))) {
        val vehicleAndKeeperDetailsModel = VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto)

        auditService1.send(AuditMessage.from(
          pageMovement = AuditMessage.VehicleLookupToVehicleLookupFailure,
          transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
          rejectionCode = Some(ErrorCodes.PostcodeMismatchErrorCode + " - vehicle_and_keeper_lookup_keeper_postcode_mismatch")))
        auditService2.send(AuditRequest.from(
          pageMovement = AuditMessage.VehicleLookupToVehicleLookupFailure,
          transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
          rejectionCode = Some(ErrorCodes.PostcodeMismatchErrorCode + " - vehicle_and_keeper_lookup_keeper_postcode_mismatch")))

        addDefaultCookies(Redirect(routes.VehicleLookupFailure.present()), formModel).
          withCookie(responseCodeCacheKey, "vehicle_and_keeper_lookup_keeper_postcode_mismatch")
      } else
        addDefaultCookies(Redirect(routes.CheckEligibility.present()), formModel).
          withCookie(VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto))
  }

  def back = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present())
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

  private def addDefaultCookies(result: Result, formModel: VehicleAndKeeperLookupFormModel)
                               (implicit request: Request[_]): Result = result
    .withCookie(TransactionIdCacheKey, transactionId(formModel))
    .withCookie(PaymentTransNoCacheKey, calculatePaymentTransNo)
}
