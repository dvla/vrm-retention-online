package controllers

import composition.TestConfig
import helpers.TestWithApplication
import controllers.Common.PrototypeHtml
import helpers.UnitSpec
import play.api.test.FakeRequest
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.SERVICE_UNAVAILABLE
import play.api.test.Helpers.status

class MicroserviceErrorUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new TestWithApplication {
      status(present) should equal(SERVICE_UNAVAILABLE)
    }

    "display prototype message when config set to true" in new TestWithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new TestWithApplication {
      val request = FakeRequest()
      val result = microServiceErrorPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  private def microServiceErrorPrototypeNotVisible = {
    testInjector(
      new TestConfig(isPrototypeBannerVisible = false)
    ).getInstance(classOf[MicroServiceError])
  }

  private def present = microServiceError.present(FakeRequest())

  private def microServiceError = testInjector().getInstance(classOf[MicroServiceError])
}