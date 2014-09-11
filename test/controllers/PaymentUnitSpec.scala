package controllers

import helpers.vrm_retention.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import pages.vrm_retention.MockFeedbackPage
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, LOCATION, OK, defaultAwaitTimeout, status}

final class PaymentUnitSpec extends UnitSpec {

  "exit" should {

    "redirect to feedback page when storeBusinessDetailsConsent cookie does not exist" in new WithApplication {
      val result = payment.exit(FakeRequest())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MockFeedbackPage.address))
      }
    }

    "redirect to feedback page when storeBusinessDetailsConsent cookie contains false" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.storeBusinessDetailsConsent(consent = "false"))
      val result = payment.exit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MockFeedbackPage.address))
      }
    }

    "redirect to feedback page when storeBusinessDetailsConsent cookie contains true" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.storeBusinessDetailsConsent(consent = "true"))
      val result = payment.exit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MockFeedbackPage.address))
      }
    }
  }

  private lazy val payment = testInjectorOverrideDev().getInstance(classOf[Payment])
}