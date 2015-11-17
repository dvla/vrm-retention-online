package controllers

import com.google.inject.Inject
import mappings.common.ErrorCodes
import models.{CacheKeyPrefix, IdentifierCacheKey, RetainModel, VehicleAndKeeperLookupFormModel}
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat
import play.api.data.FormError
import play.api.data.{Form => PlayForm}
import play.api.mvc.{Action, Request, Result}
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichCookies
import common.clientsidesession.CookieImplicits.RichForm
import common.clientsidesession.CookieImplicits.RichResult
import common.controllers.VehicleLookupBase
import common.model.BruteForcePreventionModel
import common.model.VehicleAndKeeperDetailsModel
import common.views.constraints.Postcode.formatPostcode
import common.views.constraints.RegistrationNumber.formatVrm
import common.views.helpers.FormExtensions.formBinding
import common.views.models.DayMonthYear
import common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupDetailsDto
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupFailureResponse
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupService
import utils.helpers.Config
import views.vrm_retention.Payment.PaymentTransNoCacheKey
import views.vrm_retention.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_retention.VehicleLookup.DocumentReferenceNumberId
import views.vrm_retention.VehicleLookup.PostcodeId
import views.vrm_retention.VehicleLookup.TransactionIdCacheKey
import views.vrm_retention.VehicleLookup.VehicleAndKeeperLookupResponseCodeCacheKey
import views.vrm_retention.VehicleLookup.VehicleRegistrationNumberId
import webserviceclients.audit2.AuditRequest

final class VehicleLookup @Inject()(implicit bruteForceService: BruteForcePreventionService,
                                    vehicleAndKeeperLookupService: VehicleAndKeeperLookupService,
                                    dateService: common.services.DateService,
                                    auditService2: webserviceclients.audit2.AuditService,
                                    clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config) extends VehicleLookupBase[VehicleAndKeeperLookupFormModel] {

  val unhandledVehicleAndKeeperLookupExceptionResponseCode = "VMPR6"
  val directToPaperResponseCodeText = "vrm_retention_eligibility_direct_to_paper"
  val postcodeMismatchResponseCodeText = "vehicle_and_keeper_lookup_keeper_postcode_mismatch"

  override val form = PlayForm(
    VehicleAndKeeperLookupFormModel.Form.Mapping
  )
  override val responseCodeCacheKey: String = VehicleAndKeeperLookupResponseCodeCacheKey

  override def vrmLocked(bruteForcePreventionModel: BruteForcePreventionModel,
                         formModel: VehicleAndKeeperLookupFormModel)
                        (implicit request: Request[_]): Result = {

    val vehicleAndKeeperDetailsModel = VehicleAndKeeperDetailsModel(
      registrationNumber = formatVrm(formModel.registrationNumber),
      make = None,
      model = None,
      title = None,
      firstName = None,
      lastName = None,
      address = None,
      disposeFlag = None,
      keeperEndDate = None,
      keeperChangeDate = None,
      suppressedV5Flag = None
    )

    val trackingId = request.cookies.trackingId()
    auditService2.send(AuditRequest.from(
      trackingId = trackingId,
      pageMovement = AuditRequest.VehicleLookupToVehicleLookupFailure,
      transactionId = transactionId(formModel),
      timestamp = dateService.dateTimeISOChronology,
      vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
      rejectionCode = Some(ErrorCodes.VrmLockedErrorCode + " - vrm_locked")
    ), trackingId)

    addDefaultCookies(Redirect(routes.VrmLocked.present()), transactionId(formModel))
  }

  override def microServiceError(t: Throwable, formModel: VehicleAndKeeperLookupFormModel)
                                (implicit request: Request[_]): Result =
    addDefaultCookies(Redirect(routes.MicroServiceError.present()), transactionId(formModel))

  override def vehicleLookupFailure(failure: VehicleAndKeeperLookupFailureResponse,
                                    formModel: VehicleAndKeeperLookupFormModel)
                                   (implicit request: Request[_]): Result = {

    val responseCode = failure.response

    val vehicleAndKeeperDetailsModel = VehicleAndKeeperDetailsModel(
      registrationNumber = formatVrm(formModel.registrationNumber),
      make = None,
      model = None,
      title = None,
      firstName = None,
      lastName = None,
      address = None,
      disposeFlag = None,
      keeperEndDate = None,
      keeperChangeDate = None,
      suppressedV5Flag = None
    )

    val txnId = transactionId(formModel)
    val trackingId = request.cookies.trackingId()

    auditService2.send(AuditRequest.from(
      trackingId = request.cookies.trackingId(),
      pageMovement = AuditRequest.VehicleLookupToVehicleLookupFailure,
      transactionId = txnId,
      timestamp = dateService.dateTimeISOChronology,
      vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
      rejectionCode = Some(s"${responseCode.code} - ${responseCode.message}")
    ), trackingId)

    // check whether the response code is a VMPR6 code, if so redirect to DirectToPaper
    if (responseCode.code.startsWith(unhandledVehicleAndKeeperLookupExceptionResponseCode))
      addDefaultCookies(Redirect(routes.CheckEligibility.present()), txnId).withCookie(vehicleAndKeeperDetailsModel)
    else
      addDefaultCookies(Redirect(routes.VehicleLookupFailure.present()), txnId).withCookie(vehicleAndKeeperDetailsModel)
  }

  override def presentResult(implicit request: Request[_]) = {
    request.cookies.getString(IdentifierCacheKey) match {
      case Some(c) =>
        Redirect(routes.VehicleLookup.ceg())
      case None =>
        logMessage(request.cookies.trackingId(), Info, "Presenting vehicle lookup view")
        vehicleLookup
    }
  }

  def vehicleLookup(implicit request: Request[_]) =
    request.cookies.getModel[RetainModel] match {
      case Some(fulfilModel) =>
        Ok(views.html.vrm_retention.vehicle_lookup(form)).discardingCookies(removeCookiesOnExit)
      case None =>
        Ok(views.html.vrm_retention.vehicle_lookup(form.fill()))
    }

  override def invalidFormResult(invalidForm: PlayForm[VehicleAndKeeperLookupFormModel])
                                (implicit request: Request[_]): Future[Result] = Future.successful {
    BadRequest(views.html.vrm_retention.vehicle_lookup(formWithReplacedErrors(invalidForm)))
  }

  override def vehicleFoundResult(vehicleAndKeeperDetailsDto: VehicleAndKeeperLookupDetailsDto,
                                  formModel: VehicleAndKeeperLookupFormModel)
                                 (implicit request: Request[_]): Result = {

    val txnId = transactionId(formModel)

    if (!postcodesMatch(formModel.postcode, vehicleAndKeeperDetailsDto.keeperPostcode)) {
      val vehicleAndKeeperDetailsModel = VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto)
      val trackingId = request.cookies.trackingId()

      auditService2.send(AuditRequest.from(
        trackingId = trackingId,
        pageMovement = AuditRequest.VehicleLookupToVehicleLookupFailure,
        transactionId = txnId,
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
        rejectionCode = Some(ErrorCodes.PostcodeMismatchErrorCode + " - " + postcodeMismatchResponseCodeText)
      ), trackingId)

      addDefaultCookies(Redirect(routes.VehicleLookupFailure.present()), txnId).
        withCookie(responseCodeCacheKey, postcodeMismatchResponseCodeText)
    } else
      addDefaultCookies(Redirect(routes.CheckEligibility.present()), txnId).
        withCookie(VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto))
  }

  def back = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present())
  }

  val identifier = "CEG"
  def ceg = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Info, s"Presenting vehicle lookup view for identifier ${identifier}")
    vehicleLookup.withCookie(IdentifierCacheKey, identifier)
  }

  private def transactionId(validForm: VehicleAndKeeperLookupFormModel): String = {
    val transactionTimestamp =
      DayMonthYear.from(new DateTime(dateService.now, DateTimeZone.forID("Europe/London"))).toDateTimeMillis.get
    val isoDateTimeString = ISODateTimeFormat.yearMonthDay().print(transactionTimestamp).drop(2) + " " +
      ISODateTimeFormat.hourMinuteSecond().print(transactionTimestamp)
    validForm.registrationNumber +
      isoDateTimeString.replace(" ", "").replace("-", "").replace(":", "").replace(".", "")
  }

  private def formWithReplacedErrors(form: PlayForm[VehicleAndKeeperLookupFormModel])(implicit request: Request[_]) =
    (form /: List(
      (VehicleRegistrationNumberId, "error.restricted.validVrnOnly"),
      (DocumentReferenceNumberId, "error.validDocumentReferenceNumber"),
      (PostcodeId, "error.restricted.validV5CPostcode"))) { (form, error) =>
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

  private def addDefaultCookies(result: Result, transactionId: String)
                               (implicit request: Request[_]): Result = result
    .withCookie(TransactionIdCacheKey, transactionId)
    .withCookie(PaymentTransNoCacheKey, calculatePaymentTransNo)

  private def postcodesMatch(formModelPostcode: String, dtoPostcode: Option[String])
                            (implicit request: Request[_]) = {
    dtoPostcode match {
      case Some(postcode) =>
        logMessage(request.cookies.trackingId, Info, s"formModelPostcode = $formModelPostcode dtoPostcode $postcode")

        def formatPartialPostcode(postcode: String): String = {
          val SpaceCharDelimiter = " "
          val A99AA = "([A-Z][0-9][*]{3})".r
          val A099AA = "([A-Z][0][0-9][*]{3})".r
          val A999AA = "([A-Z][0-9]{2}[*]{3})".r
          val A9A9AA = "([A-Z][0-9][A-Z][*]{3})".r
          val AA99AA = "([A-Z]{2}[0-9][*]{3})".r
          val AA099AA = "([A-Z]{2}[0][0-9][*]{3})".r
          val AA999AA = "([A-Z]{2}[0-9]{2}[*]{3})".r
          val AA9A9AA = "([A-Z]{2}[0-9][A-Z][*]{3})".r

          postcode.toUpperCase.replace(SpaceCharDelimiter, "") match {
            case A99AA(p) => p.substring(0, 2)
            case A099AA(p) => p.substring(0, 1) + p.substring(2, 3)
            case A999AA(p) => p.substring(0, 3)
            case A9A9AA(p) => p.substring(0, 3)
            case AA99AA(p) => p.substring(0, 3)
            case AA099AA(p) => p.substring(0, 2) + p.substring(3, 4)
            case AA999AA(p) => p.substring(0, 4)
            case AA9A9AA(p) => p.substring(0, 4)
            case _ => formatPostcode(postcode)
          }
        }

        // strip the spaces before comparison
        formatPostcode(formModelPostcode).filterNot(" " contains _).toUpperCase ==
          formatPartialPostcode(postcode).filterNot(" " contains _).toUpperCase

      case None =>
        logMessage(request.cookies.trackingId,Info,"formModelPostcode = " + formModelPostcode)
        formModelPostcode.isEmpty
    }
  }
}
