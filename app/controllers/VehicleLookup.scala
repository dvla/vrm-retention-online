package controllers

import com.google.inject.Inject
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.mvc._
import services.vehicle_and_keeper_lookup.VehicleAndKeeperLookupService
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.constraints.Postcode.formatPostcode
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import utils.helpers.Config
import viewmodels._
import views.vrm_retention.RelatedCacheKeys
import views.vrm_retention.VehicleLookup._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class VehicleLookup @Inject()(bruteForceService: BruteForcePreventionService,
                                    vehicleAndKeeperLookupService: VehicleAndKeeperLookupService,
                                    dateService: DateService)
                                   (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config) extends Controller {

  private[controllers] val form = Form(
    VehicleAndKeeperLookupFormModel.Form.Mapping
  )

  def present = Action { implicit request =>
    Ok(views.html.vrm_retention.vehicle_lookup(form.fill())).
      discardingCookies(RelatedCacheKeys.VehicleAndKeeperLookupSet)
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => Future.successful {
        val formWithReplacedErrors = invalidForm.
          replaceError(VehicleRegistrationNumberId, FormError(key = VehicleRegistrationNumberId,
          message = "error.restricted.validVrnOnly",
          args = Seq.empty)).
          replaceError(DocumentReferenceNumberId, FormError(key = DocumentReferenceNumberId,
          message = "error.validDocumentReferenceNumber",
          args = Seq.empty)).
          replaceError(PostcodeId, FormError(key = PostcodeId,
          message = "error.restricted.validPostcode",
          args = Seq.empty)).
          distinctErrors
        BadRequest(views.html.vrm_retention.vehicle_lookup(formWithReplacedErrors))
      },
      validForm => {
        bruteForceAndLookup(convertToUpperCaseAndRemoveSpaces(validForm))
      }
    )
  }

  def back = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present())
  }

  private def convertToUpperCaseAndRemoveSpaces(model: VehicleAndKeeperLookupFormModel): VehicleAndKeeperLookupFormModel =
    model.copy(registrationNumber = model.registrationNumber.replace(" ", "").toUpperCase)

  private def bruteForceAndLookup(formModel: VehicleAndKeeperLookupFormModel)
                                 (implicit request: Request[_]): Future[Result] = {
    val transactionId = {
      val transactionTimestamp = dateService.today.toDateTimeMillis.get
      val isoDateTimeString = ISODateTimeFormat.yearMonthDay().print(transactionTimestamp) + " " +
        ISODateTimeFormat.hourMinuteSecondMillis().print(transactionTimestamp)
      formModel.registrationNumber +
        isoDateTimeString.replace(" ", "").replace("-", "").replace(":", "").replace(".", "")
    }

    bruteForceService.isVrmLookupPermitted(formModel.registrationNumber).flatMap { bruteForcePreventionViewModel =>
      // US270: The security micro-service will return a Forbidden (403) message when the vrm is locked, we have hidden that logic as a boolean.
      if (bruteForcePreventionViewModel.permitted) lookupVehicle(formModel, bruteForcePreventionViewModel, transactionId)
      else Future.successful {
        val registrationNumber = LogFormats.anonymize(formModel.registrationNumber)
        Logger.warn(s"BruteForceService locked out vrm: $registrationNumber")
        Redirect(routes.VrmLocked.present()).
          withCookie(TransactionIdCacheKey, transactionId).
          withCookie(formModel).
          withCookie(bruteForcePreventionViewModel)
      }
    } recover {
      case exception: Throwable =>
        Logger.error(
          s"Exception thrown by BruteForceService so for safety we won't let anyone through. " +
            s"Exception ${exception.getStackTraceString}"
        )
        Redirect(routes.MicroServiceError.present())
    }
  }

  private def lookupVehicle(vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
                            bruteForcePreventionModel: BruteForcePreventionModel,
                            transactionId: String)
                           (implicit request: Request[_]): Future[Result] = {

    def vehicleFoundResult(vehicleAndKeeperDetailsDto: VehicleAndKeeperDetailsDto) = {
      // Check the keeper's postcode matches the value on record. This is not calling any address lookup service.
      if (!formatPostcode(vehicleAndKeeperLookupForm.postcode).equals(formatPostcode(vehicleAndKeeperDetailsDto.keeperPostcode.get))) {
        Redirect(routes.VehicleLookupFailure.present()).
          withCookie(key = VehicleAndKeeperLookupResponseCodeCacheKey, value = "vehicle_and_keeper_lookup_keeper_postcode_mismatch").
          withCookie(TransactionIdCacheKey, transactionId)
      } else {
        Redirect(routes.CheckEligibility.present()).
          withCookie(VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto)).
          withCookie(TransactionIdCacheKey, transactionId)
      }
    }

    def vehicleNotFoundResult(responseCode: String) = {
      Logger.debug(s"VehicleAndKeeperLookup encountered a problem with request ${LogFormats.anonymize(vehicleAndKeeperLookupForm.referenceNumber)} ${LogFormats.anonymize(vehicleAndKeeperLookupForm.registrationNumber)}, redirect to VehicleAndKeeperLookupFailure")
      Redirect(routes.VehicleLookupFailure.present()).
        withCookie(key = VehicleAndKeeperLookupResponseCodeCacheKey, value = responseCode).
        withCookie(TransactionIdCacheKey, transactionId)
    }

    def microServiceErrorResult(message: String) = {
      Logger.error(message)
      Redirect(routes.MicroServiceError.present())
    }

    def createResultFromVehicleAndKeeperLookupResponse(response: VehicleAndKeeperDetailsResponse)
                                                      (implicit request: Request[_]) =
      response.responseCode match {
        case Some(responseCode) => vehicleNotFoundResult(responseCode) // There is only a response code when there is a problem.
        case None =>
          // Happy path when there is no response code therefore no problem.
          response.vehicleAndKeeperDetailsDto match {
            case Some(dto) => vehicleFoundResult(dto)
            case None => microServiceErrorResult(message = "No vehicleAndKeeperDetailsDto found")
          }
      }

    def vehicleAndKeeperLookupSuccessResponse(responseStatusVehicleAndKeeperLookupMS: Int,
                                              response: Option[VehicleAndKeeperDetailsResponse])
                                             (implicit request: Request[_]) =
      responseStatusVehicleAndKeeperLookupMS match {
        case OK =>
          response match {
            case Some(response) => createResultFromVehicleAndKeeperLookupResponse(response)
            case _ => microServiceErrorResult("No vehicleAndKeeperDetailsResponse found")
          }
        case _ => microServiceErrorResult(s"VehicleAndKeeperLookup web service call http status not OK, it was: $responseStatusVehicleAndKeeperLookupMS. Problem may come from either vehicle-lookup micro-service or the VSS")
      }

    val trackingId = request.cookies.trackingId()
    val vehicleAndKeeperDetailsRequest = VehicleAndKeeperDetailsRequest.from(vehicleAndKeeperLookupForm)

    vehicleAndKeeperLookupService.invoke(vehicleAndKeeperDetailsRequest, trackingId).map {
      case (responseStatusVehicleAndKeeperLookupMS: Int, vehicleAndKeeperDetailsResponse: Option[VehicleAndKeeperDetailsResponse]) =>
        vehicleAndKeeperLookupSuccessResponse(
          responseStatusVehicleAndKeeperLookupMS = responseStatusVehicleAndKeeperLookupMS,
          response = vehicleAndKeeperDetailsResponse).
          withCookie(vehicleAndKeeperLookupForm).
          withCookie(bruteForcePreventionModel).
          withCookie(TransactionIdCacheKey, transactionId)
    }.recover {
      case e: Throwable => microServiceErrorResult(message = s"VehicleAndKeeperLookup Web service call failed. Exception " + e.toString.take(45))
    }
  }
}
