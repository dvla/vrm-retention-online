package controllers.disposal_of_vehicle

import play.api.data.Form
import play.api.data.Forms._
import play.api.Logger
import play.api.mvc._
import mappings.disposal_of_vehicle.Dispose._
import mappings.common.{Mileage, DayMonthYear}
import Mileage._
import DayMonthYear._
import constraints.common
import common.DayMonthYear._
import controllers.disposal_of_vehicle.Helpers._
import models.domain.disposal_of_vehicle.{VehicleDetailsModel, DealerDetailsModel, DisposeFormModel, DisposeModel}
import models.domain.disposal_of_vehicle.DisposeViewModel
import scala.Some
import com.google.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global


class Dispose @Inject()(webService: services.DisposeService) extends Controller {

  val disposeForm = Form(
    mapping(
      mileageId -> mileage(),
      dateOfDisposalId -> dayMonthYear.verifying(validDate),
      emailAddressId -> optional(text)
    )(DisposeFormModel.apply)(DisposeFormModel.unapply)
  )

  def present = Action {
    implicit request => {
      (fetchDealerDetailsFromCache, fetchVehicleDetailsFromCache) match {
        case (Some(dealerDetails), Some(vehicleDetails)) => {
          Logger.debug("found dealer details")
          // Pre-populate the form so that the consent checkbox is ticked and today's date is displayed in the date control
          //val filledForm = disposeForm.fill(DisposeFormModel(consent = "false", dateOfDisposal = models.DayMonthYear.today))
          Ok(views.html.disposal_of_vehicle.dispose(populateModelFromCachedData(dealerDetails, vehicleDetails), disposeForm))
        }
        case _ => Redirect(routes.SetUpTradeDetails.present)
      }
    }
  }

  def submit = Action.async {
    implicit request => {
      Logger.debug("Submitted dispose form...")
      disposeForm.bindFromRequest.fold(
        formWithErrors => {
          Future {
            (fetchDealerDetailsFromCache, fetchVehicleDetailsFromCache) match {
              case (Some(dealerDetails), Some(vehicleDetails)) =>
                val disposeViewModel = populateModelFromCachedData(dealerDetails, vehicleDetails)
                BadRequest(views.html.disposal_of_vehicle.dispose(disposeViewModel, formWithErrors))
              case _ =>
                Logger.error("could not find dealer details in cache on Dispose submit")
                Redirect(routes.SetUpTradeDetails.present)
            }
          }
        },
        f => {
          storeDisposeFormModelInCache(f)
          Logger.debug(s"Dispose form submitted - mileage = ${f.mileage}, disposalDate = ${f.dateOfDisposal}")
          disposeAction(webService, f)
        }
      )
    }
  }

  private def populateModelFromCachedData(dealerDetails: DealerDetailsModel, vehicleDetails: VehicleDetailsModel): DisposeViewModel = {
    DisposeViewModel(vehicleMake = vehicleDetails.vehicleMake,
      vehicleModel = vehicleDetails.vehicleModel,
      keeperName = vehicleDetails.keeperName,
      keeperAddress = vehicleDetails.keeperAddress,
      dealerName = dealerDetails.dealerName,
      dealerAddress = dealerDetails.dealerAddress)
  }

  private def disposeAction(webService: services.DisposeService, f: DisposeFormModel): Future[SimpleResult] = {
    fetchVehicleLookupDetailsFromCache match {
      case Some(vehicleLookupFormModel) => {
        val disposeModel = DisposeModel(referenceNumber = vehicleLookupFormModel.referenceNumber, registrationNumber = vehicleLookupFormModel.registrationNumber, dateOfDisposal = f.dateOfDisposal )
        storeDisposeModelInCache(disposeModel)
        webService.invoke(disposeModel).map {
          resp =>
            Logger.debug(s"Dispose Web service call successful - response = ${resp}")
            if (resp.success) {
              storeDisposeTransactionIdInCache(resp.transactionId)
              storeDisposeRegistrationNumberInCache(resp.registrationNumber)
              Redirect(routes.DisposeSuccess.present)
            }
            else Redirect(routes.DisposeFailure.present)
        }.recoverWith {
          case e: Throwable => Future {
            Logger.debug(s"Web service call failed. Exception: ${e}")
            BadRequest("The remote server didn't like the request.") // TODO check with BAs what we want to display when the webservice throws exception. We cannot proceed so need to say something like "".
          }
        }
      }
      case _ => Future {
        Logger.error("could not find dealer details in cache on Dispose submit")
        Redirect(routes.SetUpTradeDetails.present)
      }
    }
  }
}