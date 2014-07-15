package controllers.vrm_retention

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.Logger
import mappings.common.{DocumentReferenceNumber, VehicleRegistrationNumber}
import DocumentReferenceNumber._
import VehicleRegistrationNumber._
import mappings.vrm_retention.KeeperConsent
import KeeperConsent._
import models.domain.vrm_retention._
import scala.concurrent.{Await, ExecutionContext, Future}
import ExecutionContext.Implicits.global
import com.google.inject.Inject
import services.vehicle_lookup.VehicleLookupService
import utils.helpers.FormExtensions._
import models.domain.vrm_retention.VehicleLookupFormModel
import services.brute_force_prevention.BruteForcePreventionService
import common.{LogFormats, ClientSideSessionFactory, CookieImplicits}
import CookieImplicits.RichSimpleResult
import CookieImplicits.RichCookies
import CookieImplicits.RichForm
import mappings.vrm_retention.VehicleLookup._
import play.api.data.FormError
import scala.Some
import play.api.mvc.SimpleResult
import mappings.common.Postcode.postcode
import models.domain.common._
import play.api.data.FormError
import scala.Some
import play.api.mvc.SimpleResult
import utils.helpers.Config

final class VehicleLookup @Inject()(bruteForceService: BruteForcePreventionService,
                                    vehicleLookupService: VehicleLookupService)
                                   (implicit clientSideSessionFactory: ClientSideSessionFactory, config: Config) extends Controller {

  private[vrm_retention] val form = Form(
    mapping(
      DocumentReferenceNumberId -> referenceNumber,
      VehicleRegistrationNumberId -> registrationNumber,
      PostcodeId -> postcode,
      KeeperConsentId -> keeperConsent
    )(VehicleLookupFormModel.apply)(VehicleLookupFormModel.unapply)
  )

  def present = Action {
    implicit request =>
      Ok(views.html.vrm_retention.vehicle_lookup(form.fill()))
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
                Future {
                  val formWithReplacedErrors = invalidForm.
                    replaceError(VehicleRegistrationNumberId, FormError(key = VehicleRegistrationNumberId, message = "error.restricted.validVrnOnly", args = Seq.empty)).
                    replaceError(DocumentReferenceNumberId, FormError(key = DocumentReferenceNumberId, message = "error.validDocumentReferenceNumber", args = Seq.empty)).
                    replaceError(PostcodeId, FormError(key = PostcodeId, message = "address.postcode.validation", args = Seq.empty)).
                    distinctErrors
                  BadRequest(views.html.vrm_retention.vehicle_lookup(formWithReplacedErrors))
                },
      validForm => {
        bruteForceAndLookup(convertToUpperCaseAndRemoveSpaces(validForm))
      }
    )
  }

  def back = Action {
    implicit request =>
      Redirect(routes.BeforeYouStart.present())
  }

  private def convertToUpperCaseAndRemoveSpaces(model: VehicleLookupFormModel): VehicleLookupFormModel =
    model.copy(registrationNumber = model.registrationNumber.replace(" ", "").toUpperCase)

  private def bruteForceAndLookup(formModel: VehicleLookupFormModel)
                                 (implicit request: Request[_]): Future[SimpleResult] =

    bruteForceService.isVrmLookupPermitted(formModel.registrationNumber).flatMap { bruteForcePreventionViewModel =>
    // TODO US270 @Lawrence please code review the way we are using map, the lambda (I think we could use _ but it looks strange to read) and flatmap
    // US270: The security micro-service will return a Forbidden (403) message when the vrm is locked, we have hidden that logic as a boolean.
      if (bruteForcePreventionViewModel.permitted) lookupVehicle(formModel, bruteForcePreventionViewModel)
      else Future {
        val registrationNumber = LogFormats.anonymize(formModel.registrationNumber)
        Logger.warn(s"BruteForceService locked out vrm: $registrationNumber")
        Redirect(routes.VrmLocked.present()).
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

  private def lookupVehicle(vehicleLookupFormModel: VehicleLookupFormModel, bruteForcePreventionViewModel: BruteForcePreventionViewModel)(implicit request: Request[_]): Future[SimpleResult] = {

    def vehicleFoundResult(vehicleDetailsDto: VehicleDetailsDto) = {

      // check the keeper's postcode matches
      // TODO call the vehicle and keeper lookup but for now use the VM service and check if
      // the postcode equals the 'canned' postocde that returns a failure
      if (vehicleLookupFormModel.postcode.equals("AA11AA")) {
        //      if (model.postcode.equals(vehicleDetailsDto.keeperPostcode)) {
        Redirect(routes.VehicleLookupFailure.present()).
          withCookie(key = VehicleLookupResponseCodeCacheKey, value = "vehicle_lookup_keeper_postcode_mismatch")
      } else {
        Redirect(routes.CheckEligibility.present()).
          withCookie(VehicleDetailsModel.fromDto(vehicleDetailsDto)).
          withCookie(KeeperDetailsModel.fromResponse("Mr","David", "Jones","1 High Street","Skewen","Swansea",vehicleLookupFormModel.postcode))
      }

    }

    def vehicleNotFoundResult(responseCode: String) = {
      Logger.debug(s"VehicleLookup encountered a problem with request ${LogFormats.anonymize(vehicleLookupFormModel.referenceNumber)} ${LogFormats.anonymize(vehicleLookupFormModel.registrationNumber)}, redirect to VehicleLookupFailure")
      Redirect(routes.VehicleLookupFailure.present()).
        withCookie(key = VehicleLookupResponseCodeCacheKey, value = responseCode)
    }

    def microServiceErrorResult(message: String) = {
      Logger.error(message)
      Redirect(routes.MicroServiceError.present())
    }

    def createResultFromVehicleLookupResponse(vehicleDetailsResponse: VehicleDetailsResponse)(implicit request: Request[_]) =
      vehicleDetailsResponse.responseCode match {
        case Some(responseCode) => vehicleNotFoundResult(responseCode) // There is only a response code when there is a problem.
        case None =>
          // Happy path when there is no response code therefore no problem.
          vehicleDetailsResponse.vehicleDetailsDto match {
            case Some(dto) => vehicleFoundResult(dto)
            case None => microServiceErrorResult(message = "No vehicleDetailsDto found")
          }
      }

    def vehicleLookupSuccessResponse(responseStatusVehicleLookupMS: Int,
                                     vehicleDetailsResponse: Option[VehicleDetailsResponse])(implicit request: Request[_]) =
      responseStatusVehicleLookupMS match {
        case OK =>
          vehicleDetailsResponse match {
            case Some(response) => createResultFromVehicleLookupResponse(response)
            case _ => microServiceErrorResult("No vehicleDetailsResponse found") // TODO write test to achieve code coverage.
          }
        case _ => microServiceErrorResult(s"VehicleLookup web service call http status not OK, it was: $responseStatusVehicleLookupMS. Problem may come from either vehicle-lookup micro-service or the VSS")
      }

    val trackingId = request.cookies.trackingId()

    val vehicleDetailsRequest = VehicleDetailsRequest(
      referenceNumber = vehicleLookupFormModel.referenceNumber,
      registrationNumber = vehicleLookupFormModel.registrationNumber,
      userName = "tbd" //request.cookies.getModel[TraderDetailsModel].map(_.traderName).getOrElse("")
    )

    vehicleLookupService.invoke(vehicleDetailsRequest, trackingId).map {
      case (responseStatusVehicleLookupMS: Int, vehicleDetailsResponse: Option[VehicleDetailsResponse]) =>
        vehicleLookupSuccessResponse(
          responseStatusVehicleLookupMS = responseStatusVehicleLookupMS,
          vehicleDetailsResponse = vehicleDetailsResponse).
          withCookie(vehicleLookupFormModel).
          withCookie(bruteForcePreventionViewModel)
    }.recover {
      case e: Throwable => microServiceErrorResult(message = s"VehicleLookup Web service call failed. Exception " + e.toString.take(45))
    }
  }

}
