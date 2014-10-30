package controllers

import composition.{TestAuditService, TestConfig}
import controllers.Common.PrototypeHtml
import helpers.vrm_retention.CookieFactoryForUnitSpecs._
import helpers.{UnitSpec, WithApplication}
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
      val result = paymentPreventBackNotVisible.present()(FakeRequest())
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
        keeperEmail(),
        paymentModel(),
        eligibilityModel()
      )
  }

  private lazy val paymentPreventBack = testInjector(new TestAuditService).getInstance(classOf[PaymentPreventBack])
  private lazy val paymentPreventBackNotVisible =
    testInjector(
      new TestConfig(isPrototypeBannerVisible = false),
      new TestAuditService
    ).getInstance(classOf[PaymentPreventBack])
}