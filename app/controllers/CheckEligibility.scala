package controllers

import com.google.inject.Inject
import views.vrm_retention.VehicleLookup
import VehicleLookup._
import viewmodels._
import play.api.Logger
import play.api.mvc._
import services.vrm_retention_eligibility.VRMRetentionEligibilityService
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichSimpleResult}
import utils.helpers.Config
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class CheckEligibility @Inject()(vrmRetentionEligibilityService: VRMRetentionEligibilityService)
                                      (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  private final val KeeperConsent = "Keeper" // TODO please move to a common place such as the mapping file.

  def present = Action.async {
    implicit request =>
      request.cookies.getModel[VehicleAndKeeperLookupFormModel] match {
        case Some(form) => checkVrmEligibility(form)
        case _ => Future {
          Redirect(routes.MicroServiceError.present())
        }
      }
  }

  /**
   * Call the eligibility service to determine if the VRM is valid for retention and a replacement mark can
   * be found.
   */
  private def checkVrmEligibility(vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel)
                                 (implicit request: Request[_]): Future[SimpleResult] = {

    def eligibilitySuccess(currentVRM: String, replacementVRM: String) = {

      if (vehicleAndKeeperLookupFormModel.consent == KeeperConsent) {
        Redirect(routes.Confirm.present()).
          withCookie(EligibilityModel.from(replacementVRM))
      } else {
        Redirect(routes.SetUpBusinessDetails.present()).
          withCookie(EligibilityModel.from(replacementVRM))
      }
    }

    def eligibilityFailure(responseCode: String) = {
      Logger.debug(s"VRMRetentionEligibility encountered a problem with request ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.referenceNumber)} ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.registrationNumber)}, redirect to VehicleLookupFailure")
      Redirect(routes.VehicleLookupFailure.present()).
        withCookie(key = VehicleAndKeeperLookupResponseCodeCacheKey, value = responseCode)
    }

    def microServiceErrorResult(message: String) = {
      Logger.error(message)
      Redirect(routes.MicroServiceError.present())
    }

    def createResultFromVRMRetentionEligibilityResponse(vrmRetentionEligibilityResponse: VRMRetentionEligibilityResponse)
                                                       (implicit request: Request[_]) =
      vrmRetentionEligibilityResponse.responseCode match {
        case Some(responseCode) => eligibilityFailure(responseCode) // There is only a response code when there is a problem.
        case None =>
          // Happy path when there is no response code therefore no problem.
          (vrmRetentionEligibilityResponse.currentVRM, vrmRetentionEligibilityResponse.replacementVRM) match {
            case (Some(currentVRM), Some(replacementVRM)) => eligibilitySuccess(currentVRM, replacementVRM)
            case (Some(currentVRM), None) => microServiceErrorResult(message = "No replacement VRM found")
            case (None, Some(replacementVRM)) => microServiceErrorResult(message = "No current VRM found")
            case _ => microServiceErrorResult(message = "Current vrm and Replacement VRM not found in response")
          }
      }

    def vrmRetentionEligibilitySuccessResponse(responseStatusVRMRetentionEligibilityMS: Int,
                                               vrmRetentionEligibilityResponse: Option[VRMRetentionEligibilityResponse])
                                              (implicit request: Request[_]) =
      responseStatusVRMRetentionEligibilityMS match {
        case OK =>
          vrmRetentionEligibilityResponse match {
            case Some(response) => createResultFromVRMRetentionEligibilityResponse(response)
            case _ => microServiceErrorResult("No vrmRetentionEligibilityResponse found") // TODO write test to achieve code coverage.
          }
        case _ => microServiceErrorResult(s"VRM Retention Eligibility Response web service call http status not OK, it was: $responseStatusVRMRetentionEligibilityMS. Problem may come from either vrm-retention-eligibility micro-service or the VSS")
      }

    val trackingId = request.cookies.trackingId()

    val vrmRetentionEligibilityRequest = VRMRetentionEligibilityRequest(
      currentVRM = vehicleAndKeeperLookupFormModel.registrationNumber,
      docRefNumber = vehicleAndKeeperLookupFormModel.referenceNumber
    )

    vrmRetentionEligibilityService.invoke(vrmRetentionEligibilityRequest, trackingId).map {
      case (responseStatusVRMRetentionEligibilityMS: Int, vrmRetentionEligibilityResponse: Option[VRMRetentionEligibilityResponse]) =>
        vrmRetentionEligibilitySuccessResponse(
          responseStatusVRMRetentionEligibilityMS = responseStatusVRMRetentionEligibilityMS,
          vrmRetentionEligibilityResponse = vrmRetentionEligibilityResponse)
    }.recover {
      case e: Throwable =>
        Logger.debug(s"VRM Retention Eligibility Web service call failed. Exception " + e.toString.take(45))
        Redirect(routes.MicroServiceError.present())
    }
  }
}