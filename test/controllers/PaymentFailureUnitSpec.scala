package controllers

import composition.WithApplication
import helpers.UnitSpec
import helpers.vrm_retention.CookieFactoryForUnitSpecs
import pages.vrm_retention.{BeforeYouStartPage, VehicleLookupPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._

final class PaymentFailureUnitSpec extends UnitSpec {

  "present" should {

    "redirect to BeforeYouStart page when TransactionId cookie not present" in new WithApplication {
      val result = paymentFailure.present(FakeRequest())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }

    "redirect to BeforeYouStart page when VehicleAndKeeperLookupFormModel cookie not present" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId())
      val result = paymentFailure.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }

    "display page when required cookies are present" in new WithApplication {
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

    "redirect to BeforeYouStart page when VehicleAndKeeperLookupFormModel cookie not present" in new WithApplication {
      val request = FakeRequest()
      val result = paymentFailure.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }

    "redirect to VehicleLookup page when required cookie is present" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel())
      val result = paymentFailure.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }
  }

  private lazy val paymentFailure = testInjector().getInstance(classOf[PaymentFailure])
}