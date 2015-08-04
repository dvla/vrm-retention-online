package views.vrm_retention

import models.CacheKeyPrefix
import play.api.Logger
import play.api.http.HeaderNames.REFERER
import play.api.mvc.Request
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel.bruteForcePreventionViewModelCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel.vehicleAndKeeperLookupDetailsCacheKey
import views.vrm_retention.BusinessDetails.BusinessDetailsCacheKey
import views.vrm_retention.CheckEligibility.CheckEligibilityCacheKey
import views.vrm_retention.Confirm.ConfirmCacheKey
import views.vrm_retention.ConfirmBusiness.StoreBusinessDetailsCacheKey
import views.vrm_retention.Payment.PaymentDetailsCacheKey
import views.vrm_retention.Retain.RetainCacheKey
import views.vrm_retention.Retain.RetainResponseCodeCacheKey
import views.vrm_retention.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import views.vrm_retention.VehicleLookup.VehicleAndKeeperLookupFormModelCacheKey
import views.vrm_retention.VehicleLookup.VehicleAndKeeperLookupResponseCodeCacheKey

object RelatedCacheKeys extends DVLALogger {

  final val SeenCookieMessageKey = "seen_cookie_message"

  val RetainSet = Set(
    bruteForcePreventionViewModelCacheKey,
    vehicleAndKeeperLookupDetailsCacheKey,
    VehicleAndKeeperLookupResponseCodeCacheKey,
    VehicleAndKeeperLookupFormModelCacheKey,
    CheckEligibilityCacheKey,
    ConfirmCacheKey,
    REFERER,
    RetainCacheKey,
    RetainResponseCodeCacheKey,
    PaymentDetailsCacheKey
  )

  val VehicleAndKeeperLookupSet = Set(
    vehicleAndKeeperLookupDetailsCacheKey,
    VehicleAndKeeperLookupResponseCodeCacheKey,
    VehicleAndKeeperLookupFormModelCacheKey
  )

  val BusinessDetailsSet = Set(
    BusinessDetailsCacheKey,
    SetupBusinessDetailsCacheKey,
    StoreBusinessDetailsCacheKey
  )

  def removeCookiesOnExit(implicit request: Request[_], clientSideSessionFactory: ClientSideSessionFactory) = {
    val storeBusinessDetails = request.cookies.getString(StoreBusinessDetailsCacheKey).exists(_.toBoolean)

    logMessage(request.cookies.trackingId, Debug, s"*** removeCookiesOnExit keep BusinessDetails: $storeBusinessDetails")
    RelatedCacheKeys.RetainSet ++ {
      if (storeBusinessDetails) Set.empty else RelatedCacheKeys.BusinessDetailsSet
    }
  }
}