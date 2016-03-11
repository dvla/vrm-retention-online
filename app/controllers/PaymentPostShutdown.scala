package controllers

import com.google.inject.Inject
import models.CacheKeyPrefix
import models.VehicleAndKeeperLookupFormModel
import models.VehicleLookupFailureViewModel
import play.api.mvc.{Action, AnyContent, Controller, Request}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichCookies
import common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.vrm_retention.VehicleLookup.TransactionIdCacheKey

final class PaymentPostShutdown @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                            config: Config,
                                            dateService: common.services.DateService) extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel]) match {
      case (Some(transactionId), Some(vehicleAndKeeperLookupForm)) =>
        val vehicleAndKeeperDetails = request.cookies.getModel[VehicleAndKeeperDetailsModel]
        displayPaymentPostShutdown(transactionId, vehicleAndKeeperLookupForm, vehicleAndKeeperDetails)
      case _ => Redirect(routes.BeforeYouStart.present())
    }
  }

  private def displayPaymentPostShutdown(transactionId: String,
                                         vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
                                         vehicleAndKeeperDetails: Option[VehicleAndKeeperDetailsModel])
                                        (implicit request: Request[AnyContent]) = {
    val viewModel = VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm, vehicleAndKeeperDetails)

    Ok(views.html.vrm_retention.payment_post_shutdown(
      transactionId = transactionId,
      vehicleLookupFailureViewModel = viewModel,
      data = vehicleAndKeeperLookupForm
    ))
  }
}