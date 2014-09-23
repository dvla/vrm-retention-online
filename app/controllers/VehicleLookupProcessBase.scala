package controllers

import controllers.VehicleLookupProcessBase.{VehicleFound, LookupResult, MicroServiceError, VehicleNotFoundError}
import play.api.Logger
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import views.vrm_retention.VehicleLookup._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

trait VehicleLookupProcessBase extends Results {
  val vrmLocked: Call
  val vehicleLookupFailure: Call
  val microServiceError: Call
  val bruteForceService: BruteForcePreventionService

  implicit val clientSideSessionFactory: ClientSideSessionFactory

  def bruteForceAndLookup(registrationNumber: String, referenceNumber: String)(implicit request: Request[_]): Future[Result] =
    bruteForceService.isVrmLookupPermitted(registrationNumber).flatMap { bruteForcePreventionModel =>
      val resultFuture = if (bruteForcePreventionModel.permitted)
        lookupVehicle(registrationNumber, referenceNumber, bruteForcePreventionModel)
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

  protected def lookup(implicit request: Request[_]): Future[LookupResult]

  private def lookupVehicle(registrationNumber: String,
                            referenceNumber: String,
                            bruteForcePreventionModel: BruteForcePreventionModel)
                           (implicit request: Request[_]): Future[Result] =
    lookup.map {
      case MicroServiceError(message) =>
        microServiceErrorResult(message)
      case VehicleNotFoundError(responseCode) =>
        Logger.debug(s"VehicleAndKeeperLookup encountered a problem with request" +
          s" ${LogFormats.anonymize(referenceNumber)}" +
          s" ${LogFormats.anonymize(registrationNumber)}," +
          s" redirect to VehicleAndKeeperLookupFailure")
        Redirect(vehicleLookupFailure).
          withCookie(VehicleAndKeeperLookupResponseCodeCacheKey, responseCode)
      case VehicleFound(result) =>
        result
    } recover {
      case NonFatal(e) =>
        microServiceErrorResult("VehicleAndKeeperLookup Web service call failed. Exception " + e.toString.take(45))
    }

  private def microServiceErrorResult(message: String): Result = {
    Logger.error(message)
    Redirect(microServiceError)
  }
}

object VehicleLookupProcessBase {
  sealed trait LookupResult

  case class MicroServiceError(message: String) extends LookupResult

  case class VehicleNotFoundError(responseCode: String) extends LookupResult
  
  case class VehicleFound(result: Result) extends LookupResult
}
