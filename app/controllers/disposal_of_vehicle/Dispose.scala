package controllers.disposal_of_vehicle

import com.google.inject.Inject
import common.ClientSideSessionFactory
import common.CookieImplicits.{RichCookies, RichForm, RichSimpleResult}
import constraints.common.DayMonthYear.{validDate, after, notInFuture}
import mappings.common.Consent.consent
import mappings.common.DayMonthYear.dayMonthYear
import mappings.common.PreventGoingToDisposePage.PreventGoingToDisposePageCacheKey
import mappings.common.Mileage.mileage
import mappings.disposal_of_vehicle.Dispose.MileageId
import mappings.disposal_of_vehicle.Dispose.DateOfDisposalId
import mappings.disposal_of_vehicle.Dispose.DateOfDisposalYearsIntoThePast
import mappings.disposal_of_vehicle.Dispose.ConsentId
import mappings.disposal_of_vehicle.Dispose.LossOfRegistrationConsentId
import mappings.disposal_of_vehicle.Dispose.DisposeFormTransactionIdCacheKey
import mappings.disposal_of_vehicle.Dispose.DisposeFormRegistrationNumberCacheKey
import mappings.disposal_of_vehicle.Dispose.DisposeFormTimestampIdCacheKey
import models.domain.disposal_of_vehicle.DisposeFormModel
import models.domain.disposal_of_vehicle.TraderDetailsModel
import models.domain.common.VehicleDetailsModel
import models.domain.disposal_of_vehicle.DisposeViewModel
import models.domain.disposal_of_vehicle.DisposeRequest
import models.domain.disposal_of_vehicle.DisposeResponse
import models.domain.disposal_of_vehicle.DisposeModel
import models.domain.disposal_of_vehicle.DisposalAddressDto
import models.domain.disposal_of_vehicle.VehicleLookupFormModel
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import play.api.data.Forms.mapping
import play.api.data.{Form, FormError}
import play.api.mvc._
import services.DateService
import services.dispose_service.DisposeService
import utils.helpers.Config
import utils.helpers.FormExtensions.formBinding
import views.html.disposal_of_vehicle.dispose

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class Dispose @Inject()(webService: DisposeService, dateService: DateService)
                             (implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {

  private[disposal_of_vehicle] val form = Form(
    mapping(
      MileageId -> mileage(),
      DateOfDisposalId -> dayMonthYear.verifying(validDate(),
        after(earliest = (dateService.today - DateOfDisposalYearsIntoThePast).years),
        notInFuture(dateService)),
      ConsentId -> consent,
      LossOfRegistrationConsentId -> consent
    )(DisposeFormModel.apply)(DisposeFormModel.unapply)
  )

  def present = Action { implicit request =>
    (request.cookies.getModel[TraderDetailsModel], request.cookies.getString(PreventGoingToDisposePageCacheKey)) match {
      case (Some(traderDetails), None) =>
        request.cookies.getModel[VehicleDetailsModel] match {
          case (Some(vehicleDetails)) =>
            val disposeViewModel = createViewModel(traderDetails, vehicleDetails)
            Ok(dispose(disposeViewModel, form.fill(), dateService))
          case _ => Redirect(routes.VehicleLookup.present())
        }
      case (_, Some(interstitial)) =>
        // US320 Kick them back to the VehicleLookup page if they arrive here by any route other that clicking the
        // "Exit" or "New Dispose" buttons.
        Redirect(routes.VehicleLookup.present()).
          discardingCookie(PreventGoingToDisposePageCacheKey)
      case _ => Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        Future {
          (request.cookies.getModel[TraderDetailsModel], request.cookies.getModel[VehicleDetailsModel]) match {
            case (Some(traderDetails), Some(vehicleDetails)) =>
              val disposeViewModel = createViewModel(traderDetails, vehicleDetails)
              BadRequest(dispose(disposeViewModel, formWithReplacedErrors(invalidForm), dateService))
            case _ =>
              Logger.debug("Could not find expected data in cache on dispose submit - now redirecting...")
              Redirect(routes.SetUpTradeDetails.present())
          }
        },
      validForm => {
        request.cookies.getString(PreventGoingToDisposePageCacheKey) match {
          case Some(_) => Future {
            Redirect(routes.VehicleLookup.present())
          } // US320 prevent user using the browser back button and resubmitting.
          case None =>
            val trackingId = request.cookies.trackingId()
            disposeAction(webService, validForm, trackingId)
        }
      }
    )
  }

  private def formWithReplacedErrors(form: Form[DisposeFormModel])(implicit request: Request[_]) = {
    // When the user doesn't select a value from the drop-down then the mapping will fail to match on an Int before
    // it gets to the constraints, so we need to replace the error type with one that will give a relevant message.
    val dateOfDisposalError = FormError("dateOfDisposal", "error.dateOfDisposal")
    form.replaceError("dateOfDisposal.day", dateOfDisposalError)
      .replaceError("dateOfDisposal.month", dateOfDisposalError)
      .replaceError("dateOfDisposal.year", dateOfDisposalError)
      .replaceError("dateOfDisposal", dateOfDisposalError)
      .replaceError(
        ConsentId,
        "error.required",
        FormError(key = ConsentId, message = "disposal_dispose.consent.notgiven", args = Seq.empty)
      ).replaceError(
        LossOfRegistrationConsentId,
        "error.required",
        FormError(key = LossOfRegistrationConsentId,
        message = "disposal_dispose.loss_of_registration.consent.notgiven",
        args = Seq.empty)
      ).distinctErrors
  }

  private def createViewModel(traderDetails: TraderDetailsModel,
                              vehicleDetails: VehicleDetailsModel): DisposeViewModel =
    DisposeViewModel(
      registrationNumber = vehicleDetails.registrationNumber,
      vehicleMake = vehicleDetails.vehicleMake,
      vehicleModel = vehicleDetails.vehicleModel,
      dealerName = traderDetails.traderName,
      dealerAddress = traderDetails.traderAddress)

  private def disposeAction(webService: DisposeService, disposeFormModel: DisposeFormModel, trackingId: String)
                           (implicit request: Request[AnyContent]): Future[SimpleResult] = {

    def nextPage(httpResponseCode: Int, response: Option[DisposeResponse]) =
    // This makes the choice of which page to go to based on the first one it finds that is not None.
      response match {
        case Some(r) if r.responseCode.isDefined => handleResponseCode(r.responseCode.get)
        case _ => handleHttpStatusCode(httpResponseCode)
      }

    def callMicroService(disposeModel: DisposeModel, traderDetails: TraderDetailsModel) = {
      val disposeRequest = buildDisposeMicroServiceRequest(disposeModel, traderDetails)
      webService.invoke(disposeRequest, trackingId).map {
        case (httpResponseCode, response) =>
          Some(Redirect(nextPage(httpResponseCode, response))).
            map(_.withCookie(disposeModel)).
            map(_.withCookie(disposeFormModel)).
            map(storeResponseInCache(response, _)).
            map(transactionTimestamp).
            map(_.withCookie(PreventGoingToDisposePageCacheKey, "")). // US320 interstitial should redirect to DisposeSuccess.
            get
      }.recover {
        case e: Throwable =>
          Logger.warn(s"Dispose micro-service call failed.", e)
          Redirect(routes.MicroServiceError.present())
      }
    }

    def storeResponseInCache(response: Option[DisposeResponse], nextPage: SimpleResult): SimpleResult =
      response match {
        case Some(o) =>
          val nextPageWithTransactionId =
            if (!o.transactionId.isEmpty) nextPage.withCookie(DisposeFormTransactionIdCacheKey, o.transactionId)
            else nextPage

          if (!o.registrationNumber.isEmpty)
            nextPageWithTransactionId.withCookie(DisposeFormRegistrationNumberCacheKey, o.registrationNumber)
          else nextPageWithTransactionId
        case None => nextPage
      }

    def transactionTimestamp(nextPage: SimpleResult) = {
      val transactionTimestamp = dateService.today.toDateTime.get
      val formatter = ISODateTimeFormat.dateTime()
      val isoDateTimeString = formatter.print(transactionTimestamp)
      nextPage.withCookie(DisposeFormTimestampIdCacheKey, isoDateTimeString)
    }

    def buildDisposeMicroServiceRequest(disposeModel: DisposeModel,
                                        traderDetails: TraderDetailsModel): DisposeRequest = {
      val dateTime = disposeModel.dateOfDisposal.toDateTime.get
      val formatter = ISODateTimeFormat.dateTime()
      val isoDateTimeString = formatter.print(dateTime)

      DisposeRequest(referenceNumber = disposeModel.referenceNumber,
        registrationNumber = disposeModel.registrationNumber,
        traderName = traderDetails.traderName,
        traderAddress = DisposalAddressDto.from(traderDetails.traderAddress),
        dateOfDisposal = isoDateTimeString,
        transactionTimestamp = ISODateTimeFormat.dateTime().print(dateService.today.toDateTime.get),
        prConsent = disposeModel.lossOfRegistrationConsent.toBoolean,
        keeperConsent = disposeModel.consent.toBoolean,
        mileage = disposeModel.mileage
      )
    }

    def handleResponseCode(disposeResponseCode: String): Call =
      disposeResponseCode match {
        case "ms.vehiclesService.response.unableToProcessApplication" =>
          Logger.warn("Dispose soap endpoint redirecting to dispose failure page")
          routes.DisposeFailure.present()
        case "ms.vehiclesService.response.duplicateDisposalToTrade" =>
          Logger.warn("Dispose soap endpoint redirecting to duplicate disposal page")
          routes.DuplicateDisposalError.present()
        case _ =>
          Logger.warn(s"Dispose micro-service failed redirecting to error page $disposeResponseCode")
          routes.MicroServiceError.present()
      }

    def handleHttpStatusCode(statusCode: Int): Call =
      statusCode match {
        case OK => routes.DisposeSuccess.present()
        case SERVICE_UNAVAILABLE => routes.SoapEndpointError.present()
        case _ => routes.MicroServiceError.present()
      }

    (request.cookies.getModel[TraderDetailsModel], request.cookies.getModel[VehicleLookupFormModel]) match {
      case (Some(traderDetails), Some(vehicleLookup)) =>
        val disposeModel = DisposeModel(referenceNumber = vehicleLookup.referenceNumber,
          registrationNumber = vehicleLookup.registrationNumber,
          dateOfDisposal = disposeFormModel.dateOfDisposal,
          consent = disposeFormModel.consent,
          lossOfRegistrationConsent = disposeFormModel.lossOfRegistrationConsent,
          mileage = disposeFormModel.mileage)
        callMicroService(disposeModel, traderDetails)
      case _ => Future {
        Logger.error("Could not find either dealer details or VehicleLookupFormModel in cache on Dispose submit")
        Redirect(routes.SetUpTradeDetails.present())
      }
    }
  }
}