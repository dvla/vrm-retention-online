package controllers

import com.google.inject.Inject
import models.{VehicleAndKeeperLookupFormModel, VrmLockedViewModel}
import org.joda.time.DateTime
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.model.{BruteForcePreventionModel, VehicleAndKeeperDetailsModel}
import utils.helpers.Config2
import views.vrm_retention.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_retention.VehicleLookup._

final class VrmLocked @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,

                                  config2: Config2) extends Controller {

  def present = Action { implicit request =>
    val happyPath = for {
      transactionId <- request.cookies.getString(TransactionIdCacheKey)
      bruteForcePreventionModel <- request.cookies.getModel[BruteForcePreventionModel]
      viewModel <- List(
        request.cookies.getModel[VehicleAndKeeperLookupFormModel].map(m => VrmLockedViewModel(m, _: String, _: Long)),
        request.cookies.getModel[VehicleAndKeeperDetailsModel].map(m => VrmLockedViewModel(m, _: String, _: Long))
      ).flatten.headOption
    } yield {
      Logger.debug("VrmLocked - Displaying the vrm locked error page")
      val timeString = bruteForcePreventionModel.dateTimeISOChronology
      val javascriptTimestamp = DateTime.parse(timeString).getMillis
      Ok(views.html.vrm_retention.vrm_locked(transactionId, viewModel(timeString, javascriptTimestamp)))
    }

    happyPath.getOrElse {
      Logger.warn("VrmLocked - Kicking back to start page because we can't find one of the cookies")
      Redirect(routes.VehicleLookup.present())
    }
  }

  def exit = Action { implicit request =>
    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }
}
