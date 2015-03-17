package controllers

import com.google.inject.Inject
import models.VehicleAndKeeperLookupFormModel
import models.VehicleLookupFailureViewModel
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_retention.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_retention.VehicleLookup._

final class PaymentFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel]) match {
      case (Some(transactionId), Some(vehicleAndKeeperLookupForm)) =>
        val vehicleAndKeeperDetails = request.cookies.getModel[VehicleAndKeeperDetailsModel]
        displayPaymentFailure(transactionId, vehicleAndKeeperLookupForm, vehicleAndKeeperDetails)
      case _ => Redirect(routes.BeforeYouStart.present()).
        discardingCookies(removeCookiesOnExit)
    }
  }

  def submit = Action { implicit request =>
      Redirect(routes.VehicleLookup.present()).
        discardingCookies(removeCookiesOnExit)
  }

  private def displayPaymentFailure(transactionId: String,
                                    vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
                                    vehicleAndKeeperDetails: Option[VehicleAndKeeperDetailsModel]
                                     )(implicit request: Request[AnyContent]) = {
    val viewModel = vehicleAndKeeperDetails match {
      case Some(details) => VehicleLookupFailureViewModel(details)
      case None => VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm)
    }

    Ok(views.html.vrm_retention.payment_failure(
      transactionId = transactionId,
      vehicleLookupFailureViewModel = viewModel,
      data = vehicleAndKeeperLookupForm)
    ).
      discardingCookies(removeCookiesOnExit)
  }
}