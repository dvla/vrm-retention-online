package controllers

import helpers.TestWithApplication
import helpers.UnitSpec
import helpers.vrm_retention.CookieFactoryForUnitSpecs
import pages.vrm_retention.BeforeYouStartPage
import pages.vrm_retention.VehicleLookupPage
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, OK}

class PaymentFailureUnitSpec extends UnitSpec {

  "present" should {
    "redirect to BeforeYouStart page when TransactionId cookie not present" in new TestWithApplication {
      val result = paymentFailure.present(FakeRequest())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }

    "redirect to BeforeYouStart page when VehicleAndKeeperLookupFormModel cookie not present" in new TestWithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId())
      val result = paymentFailure.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }

    "display page when required cookies are present" in new TestWithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel())
      val result = paymentFailure.present(request)
      whenReady(result) { r =>
        r.header.status should equal(OK)
      }
    }
  }

  "submit" should {
    "redirect to VehicleLookup page when VehicleAndKeeperLookupFormModel cookie not present" in new TestWithApplication {
      val request = FakeRequest()
      val result = paymentFailure.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to VehicleLookup page when required cookie is present" in new TestWithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel())
      val result = paymentFailure.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }
  }

  private def paymentFailure = testInjector().getInstance(classOf[PaymentFailure])
}