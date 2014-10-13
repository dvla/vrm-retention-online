package controllers

import helpers.vrm_retention.CookieFactoryForUnitSpecs._
import helpers.{UnitSpec, WithApplication}
import pages.vrm_retention.ErrorPage
import play.api.test.FakeRequest
import play.api.test.Helpers._

final class CheckEligibilityUnitSpec extends UnitSpec {

  "present" should {

    "redirect to error page when VehicleAndKeeperLookupFormModel cookie does not exist" in new WithApplication {
      val result = checkEligibility.present(FakeRequest())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION).get.startsWith(ErrorPage.address)
      }
    }

    "redirect to error page when VehicleAndKeeperDetailsModel cookie does not exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(vehicleAndKeeperDetailsModel())
      val result = checkEligibility.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION).get.startsWith(ErrorPage.address)
      }
    }

    "redirect to error page when StoreBusinessDetailsCacheKey cookie does not exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperDetailsModel(),
          vehicleAndKeeperDetailsModel()
        )
      val result = checkEligibility.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION).get.startsWith(ErrorPage.address)
      }
    }

    "redirect to error page when TransactionIdCacheKey cookie does not exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(
          vehicleAndKeeperDetailsModel(),
          vehicleAndKeeperDetailsModel(),
          storeBusinessDetailsConsent()
        )
      val result = checkEligibility.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION).get.startsWith(ErrorPage.address)
      }
    }
  }

  private lazy val checkEligibility = testInjector().getInstance(classOf[CheckEligibility])
}