package controllers

import com.google.inject.Inject
import models.CacheKeyPrefix
import models.VehicleAndKeeperLookupFormModel
import models.VrmLockedViewModel
import org.joda.time.DateTime
import play.api.mvc.Action
import play.api.mvc.Controller
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichCookies
import common.clientsidesession.CookieImplicits.RichResult
import common.LogFormats.DVLALogger
import common.model.BruteForcePreventionModel
import common.model.VehicleAndKeeperDetailsModel
import views.vrm_retention.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_retention.VehicleLookup.TransactionIdCacheKey

final class VrmLocked @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                  config: utils.helpers.Config,
                                  dateService: common.services.DateService)
                      extends Controller with DVLALogger {

  def present = Action { implicit request =>
    val happyPath = for {
      transactionId <- request.cookies.getString(TransactionIdCacheKey)
      bruteForcePreventionModel <- request.cookies.getModel[BruteForcePreventionModel]
      viewModel <- List(
        request.cookies.getModel[VehicleAndKeeperLookupFormModel].map(m => VrmLockedViewModel(m, _: String, _: Long)),
        request.cookies.getModel[VehicleAndKeeperDetailsModel].map(m => VrmLockedViewModel(m, _: String, _: Long))
      ).flatten.headOption
    } yield {
        logMessage(request.cookies.trackingId, Debug, "VrmLocked - Displaying the vrm locked error page")
        val timeString = bruteForcePreventionModel.dateTimeISOChronology
        val javascriptTimestamp = DateTime.parse(timeString).getMillis
        Ok(views.html.vrm_retention.vrm_locked(transactionId, viewModel(timeString, javascriptTimestamp)))
      }

    happyPath.getOrElse {
      logMessage(
        request.cookies.trackingId,
        Warn,
        "VrmLocked - Kicking back to start page because we can't find one of the cookies"
      )
      Redirect(routes.VehicleLookup.present())
    }
  }

  def exit = Action { implicit request =>
    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }
}