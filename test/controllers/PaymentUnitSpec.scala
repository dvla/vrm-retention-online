package controllers

import helpers.vrm_retention.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import pages.vrm_retention.{PaymentCallbackPage, MockFeedbackPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, OK, contentAsString, _}

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

  "callback" should {
    "should return OK" in new WithApplication {
      val result = payment.callback(FakeRequest())
      whenReady(result) { r =>
        r.header.status should equal(OK)
      }
    }

    "has title" in new WithApplication {
      val result = payment.callback(FakeRequest())
      contentAsString(result) should include(PaymentCallbackPage.title)
    }
  }

  private lazy val payment = testInjector().getInstance(classOf[Payment])
}