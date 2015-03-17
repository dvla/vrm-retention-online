package controllers

import audit1.AuditMessage
import com.google.inject.Inject
import mappings.common.ErrorCodes
import models._
import org.joda.time.format.ISODateTimeFormat
import play.api.data.FormError
import play.api.data.{Form => PlayForm}
import play.api.Logger
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichForm
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupBase
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.constraints.Postcode._
import uk.gov.dvla.vehicles.presentation.common.views.constraints.Postcode._
import uk.gov.dvla.vehicles.presentation.common.views.constraints.Postcode._
import uk.gov.dvla.vehicles.presentation.common.views.constraints.Postcode.formatPostcode
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber._
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupService
import utils.helpers.Config
import views.vrm_retention.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_retention.Payment._
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
                                    config: Config) extends VehicleLookupBase[VehicleAndKeeperLookupFormModel] {

  val unhandledVehicleAndKeeperLookupExceptionResponseCode = "VMPR6"
  val directToPaperResponseCodeText = "vrm_retention_eligibility_direct_to_paper"
  val postcodeMismatchResponseCodeText = "vehicle_and_keeper_lookup_keeper_postcode_mismatch"

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

    // TODO need to change VehicleAndKeeperDetailsModel to take just a registrationNumber
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
      keeperChangeDate = None,
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

    // check whether the response code is a VMPR6 code, if so redirect to DirectToPaper
    if (responseCode.startsWith(unhandledVehicleAndKeeperLookupExceptionResponseCode)) {
      addDefaultCookies(Redirect(routes.CheckEligibility.present()), formModel)
    } else {
      addDefaultCookies(Redirect(routes.VehicleLookupFailure.present()), formModel)
    }
  }

  override def presentResult(implicit request: Request[_]) =
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

  override def vehicleFoundResult(vehicleAndKeeperDetailsDto: VehicleAndKeeperDetailsDto,
                                  formModel: VehicleAndKeeperLookupFormModel)
                                 (implicit request: Request[_]): Result = {

    if (!postcodesMatch(formModel.postcode, vehicleAndKeeperDetailsDto.keeperPostcode)) {

        val vehicleAndKeeperDetailsModel = VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto)

        auditService1.send(AuditMessage.from(
          pageMovement = AuditMessage.VehicleLookupToVehicleLookupFailure,
          transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
          rejectionCode = Some(ErrorCodes.PostcodeMismatchErrorCode + " - " + postcodeMismatchResponseCodeText)))
        auditService2.send(AuditRequest.from(
          pageMovement = AuditMessage.VehicleLookupToVehicleLookupFailure,
          transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
          rejectionCode = Some(ErrorCodes.PostcodeMismatchErrorCode + " - " + postcodeMismatchResponseCodeText)))

        addDefaultCookies(Redirect(routes.VehicleLookupFailure.present()), formModel).
          withCookie(responseCodeCacheKey, postcodeMismatchResponseCodeText)
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

  private def addDefaultCookies(result: Result, formModel: VehicleAndKeeperLookupFormModel)
                               (implicit request: Request[_]): Result = result
    .withCookie(TransactionIdCacheKey, transactionId(formModel))
    .withCookie(PaymentTransNoCacheKey, calculatePaymentTransNo)

  private def postcodesMatch(formModelPostcode: String, dtoPostcode: Option[String]) = {
    dtoPostcode match {
      case Some(postcode) => {
        Logger.info("formModelPostcode = " + formModelPostcode + " dtoPostcode " + postcode)

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
        formatPostcode(formModelPostcode).filterNot(" " contains _).toUpperCase() ==
          formatPartialPostcode(postcode).filterNot(" " contains _).toUpperCase()
      }
      case None => {
        Logger.info("formModelPostcode = " + formModelPostcode)
        formModelPostcode.isEmpty
      }
    }
  }
}
