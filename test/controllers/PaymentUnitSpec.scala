package controllers

import helpers.vrm_retention.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, WithApplication}
import pages.vrm_retention.{MicroServiceErrorPage, MockFeedbackPage, PaymentCallbackPage}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.test.Helpers.{LOCATION, OK, contentAsString, _}
import play.api.test.FakeHeaders

final class PaymentUnitSpec extends UnitSpec {

  "begin" should {

    "redirect to MicroServiceError page when TransactionId cookie does not exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel())
      val result = payment.begin(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to MicroServiceError page when VehicleAndKeeperLookupFormModel cookie does not exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId())
      val result = payment.begin(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to MicroServiceError page when no referer in request" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel())

      val result = payment.begin(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }

    "redirect to Payment page when required cookies exist" in new WithApplication {
      val referer = Seq("somewhere-made-up")
      val refererHeader = (REFERER, referer)
      val headers = FakeHeaders(data = Seq(refererHeader))
      val request = FakeRequest(method = "GET", uri = "/", headers = headers, body = AnyContentAsEmpty).
        withCookies(CookieFactoryForUnitSpecs.transactionId()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel())

      val result = payment.begin(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(MicroServiceErrorPage.address))
      }
    }
  }

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