package controllers

import composition.audit1.AuditLocalService
import composition.webserviceclients.audit2.AuditServiceDoesNothing
import composition.{TestConfig, WithApplication}
import controllers.Common.PrototypeHtml
import helpers.UnitSpec
import helpers.vrm_retention.CookieFactoryForUnitSpecs._
import pages.vrm_retention.SuccessPaymentPage
import play.api.test.FakeRequest
import play.api.test.Helpers._

final class PaymentPreventBackUnitSpec extends UnitSpec {

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