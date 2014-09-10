package controllers

import com.tzavellas.sse.guice.ScalaModule
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.vrm_retention.CookieFactoryForUnitSpecs._
import helpers.{UnitSpec, WithApplication}
import pages.vrm_retention.{PaymentPage, VehicleLookupPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, OK}
import services.fakes.AddressLookupServiceConstants.KeeperEmailValid
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieFlags
import utils.helpers.CookieFlagsRetention
import views.vrm_retention.Confirm.{KeeperEmailCacheKey, KeeperEmailId, StoreBusinessDetailsCacheKey, StoreDetailsConsentId}
import views.vrm_retention.VehicleLookup.{UserType_Business, UserType_Keeper}
import scala.concurrent.duration.DurationInt

final class ConfirmUnitSpec extends UnitSpec {

  "present" should {

    "display the page when required cookies are cached" in new WithApplication {
      whenReady(present, timeout) { r =>
        r.header.status should equal(OK)
      }
    }

    "display the page when required cookies are cached and StoreBusinessDetails cookie exists and is true" in new WithApplication {
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          storeBusinessDetailsConsent("true")
        )
      val result = confirm.present(request)
      whenReady(result, timeout) { r =>
        r.header.status should equal(OK)
      }
    }

    "redirect to VehicleLookup when required cookies do not exist" in new WithApplication {
      val request = FakeRequest()
      val result = confirm.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }
  }

  "submit" should {

    "redirect to Payment page when valid submit and user type is Business" in {
      val request = buildRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel()
        )
      val result = confirm.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentPage.address))
      }
    }

    "redirect to Payment page when valid submit and user type is Keeper" in {
      val request = buildRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Keeper),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel()
        )
      val result = confirm.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(PaymentPage.address))
      }
    }

    "write StoreBusinessDetails cookie when user type is Business and has not provided a keeperEmail" in {
      val request = buildRequest(keeperEmail = "").
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel()
        )
      val result = confirm.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(StoreBusinessDetailsCacheKey)
      }
    }

    "write StoreBusinessDetails cookie with maxAge 7 days" in {
      val expected = 7.days.toSeconds.toInt
      val request = buildRequest(keeperEmail = "").
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel()
        )
      val result = confirmWithCookieFlags.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.find(cookie => cookie.name == StoreBusinessDetailsCacheKey).get.maxAge should equal(Some(expected))
      }
    }

    "write StoreBusinessDetails cookie when user type is Business and has provided a keeperEmail" in {
      val request = buildRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          keeperEmail()
        )
      val result = confirm.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(KeeperEmailCacheKey, StoreBusinessDetailsCacheKey)
      }
    }

    "not write cookies when user type is Keeper and has not provided a keeperEmail" in {
      val request = buildRequest(keeperEmail = "").
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Keeper),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel()
        )
      val result = confirm.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should be(empty)
      }
    }

    "write KeeperEmail cookie when user type is Keeper and has provided a keeperEmail" in {
      val request = buildRequest().
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Keeper),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel()
        )
      val result = confirm.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(KeeperEmailCacheKey)
      }
    }
  }

  private lazy val present = {
    val request = FakeRequest().
      withCookies(
        vehicleAndKeeperLookupFormModel(),
        vehicleAndKeeperDetailsModel(),
        businessDetailsModel()
      )
    confirm.present(request)
  }
  private val confirm = injector.getInstance(classOf[Confirm])

  private def buildRequest(keeperEmail: String = KeeperEmailValid.get, storeDetailsConsent: Boolean = false) = {
    FakeRequest().withFormUrlEncodedBody(
      KeeperEmailId -> keeperEmail,
      StoreDetailsConsentId -> storeDetailsConsent.toString
    )
  }

  private def confirmWithCookieFlags = {
    testInjector(new ScalaModule() {
      override def configure(): Unit = {
        bind[CookieFlags].to[CookieFlagsRetention].asEagerSingleton()
      }
    }).getInstance(classOf[Confirm])
  }
}