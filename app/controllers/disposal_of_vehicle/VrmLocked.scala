package controllers.disposal_of_vehicle

import com.google.inject.Inject
import common.ClientSideSessionFactory
import common.CookieImplicits.{RichCookies, RichSimpleResult}
import mappings.disposal_of_vehicle.RelatedCacheKeys
import models.domain.disposal_of_vehicle.TraderDetailsModel
import play.api.Logger
import play.api.mvc.{Action, Controller}
import utils.helpers.Config
import models.domain.common.BruteForcePreventionViewModel

final class VrmLocked @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                  config: Config) extends Controller {

  def present = Action { implicit request =>
      request.cookies.getModel[BruteForcePreventionViewModel] match {
        case Some(viewModel) =>
          Logger.debug(s"VrmLocked - Displaying the vrm locked error page")
          Ok(views.html.disposal_of_vehicle.vrm_locked(viewModel.dateTimeISOChronology))
        case None =>
          Logger.debug("VrmLocked - Can't find cookie for BruteForcePreventionViewModel")
          Redirect(routes.VehicleLookup.present())
      }
  }

  def newDisposal = Action { implicit request =>
    request.cookies.getModel[TraderDetailsModel] match {
      case (Some(traderDetails)) =>
        Redirect(routes.VehicleLookup.present()).discardingCookies(RelatedCacheKeys.DisposeSet)
      case _ => Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def exit = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present()).discardingCookies(RelatedCacheKeys.FullSet)
  }
}