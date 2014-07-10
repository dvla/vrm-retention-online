package controllers.disposal_of_vehicle

import com.google.inject.Inject
import common.ClientSideSessionFactory
import common.CookieImplicits.{RichCookies, RichSimpleResult}
import mappings.common.PreventGoingToDisposePage.{DisposeOccurredCacheKey, PreventGoingToDisposePageCacheKey}
import mappings.disposal_of_vehicle.Dispose.DisposeFormRegistrationNumberCacheKey
import mappings.disposal_of_vehicle.Dispose.DisposeFormTransactionIdCacheKey
import mappings.disposal_of_vehicle.Dispose.SurveyRequestTriggerDateCacheKey
import mappings.disposal_of_vehicle.RelatedCacheKeys
import models.domain.disposal_of_vehicle.{DisposeFormModel, DisposeViewModel, TraderDetailsModel}
import models.domain.common.VehicleDetailsModel
import play.api.mvc.{Action, Controller, Request}
import services.DateService
import utils.helpers.Config

final class DisposeSuccess @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                     config: Config,
                                     surveyUrl: SurveyUrl,
                                     dateService: DateService) extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getModel[TraderDetailsModel],
     request.cookies.getModel[DisposeFormModel],
     request.cookies.getModel[VehicleDetailsModel],
     request.cookies.getString(DisposeFormTransactionIdCacheKey),
     request.cookies.getString(DisposeFormRegistrationNumberCacheKey)) match {
       case (Some(traderDetails),
             Some(disposeFormModel),
             Some(vehicleDetails),
             Some(transactionId),
             Some(registrationNumber)) =>
         val disposeViewModel = createViewModel(
           traderDetails,
           vehicleDetails,
           Some(transactionId),
           registrationNumber
         )
         Ok(views.html.disposal_of_vehicle.dispose_success(disposeViewModel, disposeFormModel, surveyUrl(request))).
           discardingCookies(RelatedCacheKeys.DisposeOnlySet) // TODO US320 test for this
       case _ => Redirect(routes.VehicleLookup.present()) // US320 the user has pressed back button after being on dispose-success and pressing new dispose.
     }
  }

  def newDisposal = Action { implicit request =>
    (request.cookies.getModel[TraderDetailsModel], request.cookies.getModel[VehicleDetailsModel]) match {
      case (Some(traderDetails), Some(vehicleDetails)) =>
        Redirect(routes.VehicleLookup.present()).
          discardingCookies(RelatedCacheKeys.DisposeSet).
          withCookie(PreventGoingToDisposePageCacheKey, "").
          withCookie(DisposeOccurredCacheKey, "")
      case _ => Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def exit = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present()).
      discardingCookies(RelatedCacheKeys.FullSet).
      withCookie(PreventGoingToDisposePageCacheKey, "").
      withCookie(SurveyRequestTriggerDateCacheKey, dateService.now.getMillis.toString)
  }

  private def createViewModel(traderDetails: TraderDetailsModel,
                              vehicleDetails: VehicleDetailsModel,
                              transactionId: Option[String],
                              registrationNumber: String): DisposeViewModel =
    DisposeViewModel(
      vehicleMake = vehicleDetails.vehicleMake,
      vehicleModel = vehicleDetails.vehicleModel,
      dealerName = traderDetails.traderName,
      dealerAddress = traderDetails.traderAddress,
      transactionId = transactionId,
      registrationNumber = registrationNumber
    )
}

class SurveyUrl @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                          config: Config,
                          dateService: DateService)
  extends (Request[_] => Option[String]) {

  def apply(request: Request[_]): Option[String] = {
    def url = if (!config.prototypeSurveyUrl.trim.isEmpty)
      Some(config.prototypeSurveyUrl.trim)
    else None

    request.cookies.getString(SurveyRequestTriggerDateCacheKey) match {
      case Some(lastSurveyMillis) =>
        if ((lastSurveyMillis.toLong + config.prototypeSurveyPrepositionInterval) < dateService.now.getMillis) url
        else None
      case None => url
    }
  }
}