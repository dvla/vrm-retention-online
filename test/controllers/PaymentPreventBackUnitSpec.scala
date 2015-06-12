package controllers

import composition.TestConfig
import composition.WithApplication
import controllers.Common.PrototypeHtml
import helpers.UnitSpec
import helpers.vrm_retention.CookieFactoryForUnitSpecs.confirmFormModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.eligibilityModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.paymentModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.paymentTransNo
import helpers.vrm_retention.CookieFactoryForUnitSpecs.transactionId
import helpers.vrm_retention.CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel
import helpers.vrm_retention.CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel
import pages.vrm_retention.SuccessPaymentPage
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, LOCATION, OK, status}

class PaymentPreventBackUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new WithApplication {
      val result = paymentPreventBack.present()(FakeRequest())
      status(result) should equal(OK)
    }

    "display prototype message when config set to true" in new WithApplication {
      val result = paymentPreventBack.present()(FakeRequest())
      contentAsString(result) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val result = paymentPrototypeNotVisible.present()(FakeRequest())
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "returnToSuccess" should {
    "redirect to the success payment page" in new WithApplication {
      val result = paymentPreventBack.returnToSuccess()(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION).get.startsWith(SuccessPaymentPage.address)
      }
    }
  }

  private def request = {
    FakeRequest().
      withCookies(
        transactionId(),
        paymentTransNo(),
        vehicleAndKeeperLookupFormModel(),
        vehicleAndKeeperDetailsModel(),
        confirmFormModel(),
        paymentModel(),
        eligibilityModel()
      )
  }

  private def paymentPreventBack = testInjector().getInstance(classOf[PaymentPreventBack])

  private def paymentPrototypeNotVisible =
    testInjector(
      new TestConfig(isPrototypeBannerVisible = false)
    ).getInstance(classOf[PaymentPreventBack])
}