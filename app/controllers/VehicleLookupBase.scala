package controllers

import controllers.VehicleLookupBase.{VehicleNotFound, VehicleFound, DtoMissing, LookupResult}
import play.api.Logger
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import views.vrm_retention.VehicleLookup._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

trait VehicleLookupBase extends Controller {
  val vrmLocked: Call
  val vehicleLookupFailure: Call
  val microServiceError: Call
  val bruteForceService: BruteForcePreventionService

  type Form

  implicit val clientSideSessionFactory: ClientSideSessionFactory

  def bruteForceAndLookup(registrationNumber: String, referenceNumber: String, form: Form)
                         (implicit request: Request[_]): Future[Result] =
    bruteForceService.isVrmLookupPermitted(registrationNumber).flatMap { bruteForcePreventionModel =>
      val resultFuture = if (bruteForcePreventionModel.permitted)
        lookupVehicle(registrationNumber, referenceNumber, bruteForcePreventionModel, form)
      else Future.successful {
        val anonRegistrationNumber = LogFormats.anonymize(registrationNumber)
        Logger.warn(s"BruteForceService locked out vrm: $anonRegistrationNumber")
        Redirect(vrmLocked)
      }

      resultFuture.map { result =>
        result.withCookie(bruteForcePreventionModel)
      }

    } recover {
      case exception: Throwable =>
        Logger.error(
          s"Exception thrown by BruteForceService so for safety we won't let anyone through. " +
            s"Exception ${exception.getStackTraceString}"
        )
        Redirect(microServiceError)
    }

  protected def callLookupService(trackingId: String, form: Form)(implicit request: Request[_]): Future[LookupResult]

  private def lookupVehicle(registrationNumber: String,
                            referenceNumber: String,
                            bruteForcePreventionModel: BruteForcePreventionModel,
                            form: Form)
                           (implicit request: Request[_]): Future[Result] = {
    def notFound(responseCode: String): Result = {
      Logger.debug(s"VehicleAndKeeperLookup encountered a problem with request" +
        s" ${LogFormats.anonymize(referenceNumber)}" +
        s" ${LogFormats.anonymize(registrationNumber)}," +
        s" redirect to VehicleAndKeeperLookupFailure")
      Redirect(vehicleLookupFailure).
        withCookie(VehicleAndKeeperLookupResponseCodeCacheKey, responseCode)
    }

    callLookupService(request.cookies.trackingId(), form).map {
      case VehicleNotFound(responseCode) => notFound(responseCode)
      case VehicleFound(result) => result
      case DtoMissing() => microServiceErrorResult("No DTO found")
    } recover {
      case NonFatal(e) =>
        microServiceErrorResult("VehicleAndKeeperLookup Web service call failed. Exception " + e.toString.take(45))
    }
  }

  private def microServiceErrorResult(message: String): Result = {
    Logger.error(message)
    Redirect(microServiceError)
  }
}

object VehicleLookupBase {
  sealed trait LookupResult
  
  final case class VehicleNotFound(responseCode: String) extends LookupResult

  final case class VehicleFound(result: Result) extends LookupResult

  final case class DtoMissing() extends LookupResult
}
