package controllers.disposal_of_vehicle

import com.google.inject.Inject
import common.CookieImplicits.{RichCookies, RichForm, RichSimpleResult}
import common.{ClientSideSessionFactory, LogFormats}
import mappings.common.DocumentReferenceNumber._
import mappings.common.PreventGoingToDisposePage.PreventGoingToDisposePageCacheKey
import mappings.common.VehicleRegistrationNumber._
import mappings.disposal_of_vehicle.VehicleLookup._
import models.domain.common.BruteForcePreventionResponse._
import models.domain.disposal_of_vehicle._
import play.api.Logger
import play.api.data.Forms._
import play.api.data.{Form, FormError}
import play.api.mvc._
import services.brute_force_prevention.BruteForcePreventionService
import services.vehicle_lookup.VehicleLookupService
import utils.helpers.Config
import utils.helpers.FormExtensions._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import mappings.disposal_of_vehicle.RelatedCacheKeys

final class VehicleLookup @Inject()(bruteForceService: BruteForcePreventionService, vehicleLookupService: VehicleLookupService)
                                   (implicit clientSideSessionFactory: ClientSideSessionFactory, config: Config) extends Controller {

  private[disposal_of_vehicle] val form = Form(
    mapping(
      DocumentReferenceNumberId -> referenceNumber,
      VehicleRegistrationNumberId -> registrationNumber
    )(VehicleLookupFormModel.apply)(VehicleLookupFormModel.unapply)
  )

  def present = Action { implicit request =>
    request.cookies.getModel[TraderDetailsModel] match {
      case Some(traderDetails) => Ok(views.html.disposal_of_vehicle.vehicle_lookup(traderDetails, form.fill()))
      case None => Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def submit = Action.async { implicit request =>
    val formData = request.body.asFormUrlEncoded.getOrElse(Map.empty[String, Seq[String]])
    val actionValue = formData.get("action").flatMap(_.headOption)
    actionValue match {
      case Some("lookup") =>
        vehicleLookup
      case Some("exit") =>
        exit
      case _ => Future{BadRequest(ActionNotAllowedMessage)} // TODO redirect to error page ?
    }
  }

  private def vehicleLookup(implicit request: Request[AnyContent]): Future[SimpleResult] = {
    form.bindFromRequest.fold(
      invalidForm =>
        Future {
          request.cookies.getModel[TraderDetailsModel] match {
            case Some(traderDetails) => val formWithReplacedErrors = invalidForm.
              replaceError(VehicleRegistrationNumberId, FormError(key = VehicleRegistrationNumberId, message = "error.restricted.validVrnOnly", args = Seq.empty)).
              replaceError(DocumentReferenceNumberId, FormError(key = DocumentReferenceNumberId, message = "error.validDocumentReferenceNumber", args = Seq.empty)).
              distinctErrors
              BadRequest(views.html.disposal_of_vehicle.vehicle_lookup(traderDetails, formWithReplacedErrors))
            case None => Redirect(routes.SetUpTradeDetails.present())
          }
        },
      validForm => {
        bruteForceAndLookup(convertToUpperCaseAndRemoveSpaces(validForm))
      }
    )
  }

  private def exit(implicit request: Request[AnyContent]): Future[SimpleResult] = {
    Future {Redirect(routes.BeforeYouStart.present()).
      discardingCookies(RelatedCacheKeys.FullSet)}
  }

  private def convertToUpperCaseAndRemoveSpaces(model: VehicleLookupFormModel): VehicleLookupFormModel =
    model.copy(registrationNumber = model.registrationNumber.replace(" ", "")
      .toUpperCase)

  def back = Action { implicit request =>
    request.cookies.getModel[TraderDetailsModel] match {
      case Some(dealerDetails) =>
        if (dealerDetails.traderAddress.uprn.isDefined) Redirect(routes.BusinessChooseYourAddress.present())
        else Redirect(routes.EnterAddressManually.present())
      case None => Redirect(routes.SetUpTradeDetails.present())
    }
  }

  private def bruteForceAndLookup(formModel: VehicleLookupFormModel)
                                 (implicit request: Request[_]): Future[SimpleResult] =
    bruteForceService.isVrmLookupPermitted(formModel.registrationNumber).map { response =>
      response // TODO US270 @Lawrence please code review the way we are using map, the lambda (I think we could use _ but it looks strange to read) and flatmap
    } flatMap {
      case Some(bruteForcePreventionViewModel) =>
        // US270: The security micro-service will return a Forbidden (403) message when the vrm is locked, we have hidden that logic as a boolean.
        if (bruteForcePreventionViewModel.permitted) lookupVehicleResult(formModel, bruteForcePreventionViewModel)
        else Future {
          val registrationNumber = LogFormats.anonymize(formModel.registrationNumber)
          Logger.warn(s"BruteForceService locked out vrm: $registrationNumber")
          Redirect(routes.VrmLocked.present()).
            withCookie(bruteForcePreventionViewModel)
        }
      case None => Future {
        Redirect(routes.MicroServiceError.present())
      }
    } recover {
      case exception: Throwable =>
        Logger.error(s"Exception thrown by BruteForceService so for safety we won't let anyone through. Exception ${exception.getStackTraceString}")
        Redirect(routes.MicroServiceError.present())
    }

  private def lookupVehicleResult(model: VehicleLookupFormModel, bruteForcePreventionViewModel: BruteForcePreventionViewModel)(implicit request: Request[_]): Future[SimpleResult] = {
    def vehicleFoundResult(vehicleDetailsDto: VehicleDetailsDto) =
      Redirect(routes.Dispose.present()).
        withCookie(VehicleDetailsModel.fromDto(vehicleDetailsDto)).
        discardingCookie(PreventGoingToDisposePageCacheKey) // US320: we have successfully called the lookup service so we cannot be coming back from a dispose success (as the doc id will have changed and the call sould fail).

    def vehicleNotFoundResult(responseCode: String) = {
      Logger.debug(s"VehicleLookup encountered a problem with request ${LogFormats.anonymize(model.referenceNumber)} ${LogFormats.anonymize(model.registrationNumber)}, redirect to VehicleLookupFailure")
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
      referenceNumber = model.referenceNumber,
      registrationNumber = model.registrationNumber,
      userName = request.cookies.getModel[TraderDetailsModel].map(_.traderName).getOrElse("")
    )
    vehicleLookupService.invoke(vehicleDetailsRequest, trackingId).map {
      case (responseStatusVehicleLookupMS: Int, vehicleDetailsResponse: Option[VehicleDetailsResponse]) =>
        vehicleLookupSuccessResponse(
          responseStatusVehicleLookupMS = responseStatusVehicleLookupMS,
          vehicleDetailsResponse = vehicleDetailsResponse).
          withCookie(model).
          withCookie(bruteForcePreventionViewModel)
    }.recover {
      case e: Throwable => microServiceErrorResult(message = s"VehicleLookup Web service call failed. Exception " + e.toString.take(45))
    }
  }
}
