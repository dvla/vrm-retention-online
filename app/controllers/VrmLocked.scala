package controllers

import com.google.inject.Inject
import models.CacheKeyPrefix
import models.VehicleAndKeeperLookupFormModel
import models.VrmLockedViewModel
import org.joda.time.DateTime
import play.api.mvc.{Request, Result}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichCookies
import common.clientsidesession.CookieImplicits.RichResult
import common.controllers.VrmLockedBase
import common.model.BruteForcePreventionModel
import views.vrm_retention.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_retention.VehicleLookup.TransactionIdCacheKey

final class VrmLocked @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                  config: utils.helpers.Config,
                                  dateService: common.services.DateService)
                      extends VrmLockedBase {

  protected override def presentResult(model: BruteForcePreventionModel)(implicit request: Request[_]): Result = {
    val happyPath = for {
      transactionId <- request.cookies.getString(TransactionIdCacheKey)
      viewModel <- request.cookies.getModel[VehicleAndKeeperLookupFormModel].map(m => VrmLockedViewModel(m, _: String, _: Long))
    } yield {
        logMessage(request.cookies.trackingId(), Debug, "VrmLocked - Displaying the vrm locked error page")
        val timeString = model.dateTimeISOChronology
        val javascriptTimestamp = DateTime.parse(timeString).getMillis
        Ok(views.html.vrm_retention.vrm_locked(
          transactionId,
          viewModel(timeString, javascriptTimestamp)
        ))
      }

    happyPath.getOrElse {
      logMessage(
        request.cookies.trackingId(),
        Warn,
        "VrmLocked - Kicking back to start page because we can't find one of the cookies"
      )
      Redirect(routes.VehicleLookup.present())
    }
  }

  protected override def missingBruteForcePreventionCookie(implicit request: Request[_]): Result = {
    logMessage(request.cookies.trackingId(), Debug,
      s"Missing BruceForcePreventionCookie. Redirecting to ${routes.VehicleLookup.present()}")
    Redirect(routes.VehicleLookup.present())
  }

  protected override def exitResult(implicit request: Request[_]): Result = {
    logMessage(request.cookies.trackingId(), Debug,
      s"Exiting VrmLocked. Redirecting to ${routes.BeforeYouStart.present()}")
    Redirect(routes.LeaveFeedback.present())
      .discardingCookies(removeCookiesOnExit)
  }

  // Not used for Retention
  protected override def tryAnotherResult(implicit request: Request[_]): Result = NotFound
}
